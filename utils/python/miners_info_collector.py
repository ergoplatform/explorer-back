import argparse
import yaml
import requests
import psycopg2


INFO_ROUTE = "/info"

ADDR_ROUTE = "/wallet/addresses"

QUERY_CONST = "INSERT INTO known_miners(miner_address, miner_name) VALUES"


def fetch_info(peers):
    for peer in peers:
        try:
            info_r = requests.get(peer + INFO_ROUTE)
            addr_r = requests.get(peer + ADDR_ROUTE)
            if info_r.status_code == 200 and addr_r.status_code == 200:
                yield (info_r.json()["name"], addr_r.json())
        except requests.exceptions.ConnectionError:
            print(f"ConnectionError while connecting to {peer}")


def to_insert(miner_info):
    miner_name = miner_info[0]
    data_to_insert = ""
    for addr in miner_info[1]:
        data_to_insert += f"('{addr}', '{miner_name}'),\n"
    return data_to_insert[:-2] + ";"


def load_config(path):
    with open(path, "r") as file:
        return yaml.safe_load(file)


if __name__ == "__main__":

    parser = argparse.ArgumentParser()
    parser.add_argument('config_url', type=str, default='')
    args = parser.parse_args()

    if args.config_url:

        config = load_config(args.config_url)

        miners_info = {k: v for k, v in fetch_info(config["peers"])}
        query = QUERY_CONST + "\n"
        for item in miners_info.items():
            query += to_insert(item)

        conn = psycopg2.connect(database=config["db"]["database"],
                                user=config["db"]["user"],
                                password=config["db"]["password"],
                                host=config["db"]["host"],
                                port=config["db"]["port"])

        cursor = conn.cursor()
        cursor.execute(query)

        conn.commit()
        conn.close()

        print("{} record(s) commited".format(sum([len(arr) for arr in miners_info.values()])))

        exit(0)

    else:
        print("Config path not specified")
        exit(1)
