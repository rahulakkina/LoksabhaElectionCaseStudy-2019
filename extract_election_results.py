import logging
import codecs
import json
import requests
import pandas as pd
from bs4 import BeautifulSoup


# Loads json configuration from the configuration file.
def get_config(conf_path):
    with codecs.open(conf_path, 'r', 'utf-8-sig') as json_data:
        d = json.load(json_data)
        return d


def create_df(file_name):
    return pd.read_csv(file_name, header='infer')


def get_value(df, t):
    for index, row in df.iterrows():
        if row[t[1]] == t[0]:
            return row[t[2]]
    return t[3]


logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)

cfg = get_config("config/cfg.json")


def get_constituency_results(constituency_code, constituency_name, state_code, state_name):

    url = cfg["ECI_RESULTS_URL"] %(state_code, constituency_code)
    response = requests.get(url, params={"ac": constituency_code}, proxies=cfg['PROXY'])
    result_set = []

    if response.status_code == 200:
        html_parser = BeautifulSoup(response.text, 'lxml')
        table = html_parser.find_all('table')[10]

        if table is not None:

            row_marker = 0
            tr_s = table.find_all('tr')
            l_tr_s = len(tr_s)

            for tr in tr_s:
                if row_marker > 2 and row_marker < (l_tr_s - 2):
                    td = tr.find_all('td')
                    row = {
                        "CANDIDATE_NAME": td[1].get_text().strip().upper(),
                        "PARTY": td[2].get_text().strip(),
                        "CONSTITUENCY_NAME": constituency_name,
                        "CONSTITUENCY_CODE": constituency_code,
                        "STATE_NAME": state_name,
                        "STATE_CODE": state_code,
                        "EVM_VOTES": int(td[3].get_text().strip()),
                        "POSTAL VOTES": int(td[5].get_text().strip()) - int(td[3].get_text().strip()),
                        "TOTAL_VOTES": int(td[5].get_text().strip()),
                        "VOTING_PERCENTAGE": float(td[6].get_text().strip())
                    }
                    result_set.append(row)
                row_marker += 1
    logging.info("Code : %s%s, Constituency : %s, State : %s, Size : %d" %(state_code, constituency_code,
                                                                           constituency_name, state_name,
                                                                           len(result_set)))
    return result_set


def extract_voting_results():

    states_df = create_df(cfg["INPUT_DATA_SRC"]["STATES_INDEX"])
    constituency_df = create_df(cfg["INPUT_DATA_SRC"]["CONSTITUENCIES"])
    result_lst = []

    for index, row in constituency_df.iterrows():
        state_code = get_value(states_df, [row['STATE'], 'STATE', 'STATE_CODE'])
        result_lst.extend(get_constituency_results(row["POSTFIX_CODE"], row["CONSTITUENCY"], state_code, row['STATE']))

    voting_results_df = pd.DataFrame.from_records(result_lst)
    voting_results_df.to_csv(cfg["OUTPUT_DATA_SRC"]["VOTING_RESULTS"]["CSV"], index=False, header=True)
    voting_results_df.to_json(cfg["OUTPUT_DATA_SRC"]["VOTING_RESULTS"]["JSON"], orient='records')

    return voting_results_df


''' Main '''

extract_voting_results()



