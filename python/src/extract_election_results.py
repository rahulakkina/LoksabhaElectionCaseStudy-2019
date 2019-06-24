import logging
import codecs
import pandas as pd
import json
import requests
import feedparser
import difflib
from sortedcontainers import SortedSet
from bs4 import BeautifulSoup
from functools import lru_cache

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


def get_recontest_candidates(url, params):
        re_contest_candidates = SortedSet()
        response = requests.get(url, params=params, proxies=cfg['PROXY'])
        if response.status_code == 200:
            html_parser = BeautifulSoup(response.text, 'lxml')
            table = html_parser.find_all('table')[2]
            row_marker = 0
            for row in table.find_all('tr'):
                if row_marker > 2:
                    td = row.find_all('td')
                    if len(td) > 1:
                        href = td[1].find("a").get("href").split("&")[2]
                        re_contest_candidates.add(href.split("=")[1])
                row_marker += 1
        return re_contest_candidates


logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)

cfg = get_config("config/cfg.json")


candidate_analysis_df = create_df(cfg["OUTPUT_DATA_SRC"]["CANDIDATE_ANALYSED_LIST"]['CSV'])

voting_results_df = create_df(cfg["OUTPUT_DATA_SRC"]["VOTING_RESULTS"]['CSV'])

age_df = create_df(cfg['INPUT_DATA_SRC']['AGE_INDEX'])


def get_age_related_points(age):
    for index, row in age_df.iterrows():
          if row['FROM'] <= age and row['TO'] >= age:
              return row['POINTS']

@lru_cache(maxsize=3000)
def get_media_popularity_score(name):

    response = requests.get(cfg["NEWS_URL"], params={"q": '"%s"' % name,
                                                   "hl": "en-SG", "gl": "SG", "ceid": "SG:en"}, proxies=cfg['PROXY'])

    if response.status_code == 200:
        d = feedparser.parse(response.text)
        return round(len(d['entries']) * 0.01, 3)
    else:
        return 0.000

result_dict = {}

recontesting_candidates = get_recontest_candidates(cfg["DATA_SET_URL"], cfg["RECONTEST_DATA_SET_PARAMS"])
state_df = create_df(cfg['INPUT_DATA_SRC']['STATES_INDEX'])
party_df = create_df(cfg['INPUT_DATA_SRC']['POLITICAL_PARTY_INDEX'])
constituency_df = create_df(cfg['INPUT_DATA_SRC']['CONSTITUENCIES'])


for idx, r in voting_results_df.iterrows():
    if r["CONSTITUENCY_NAME"] not in result_dict:
        result_dict[r["CONSTITUENCY_NAME"]] = {}
    result_dict[r["CONSTITUENCY_NAME"]][r["CANDIDATE_NAME"]] = r

result_count = 0
closest_count = 0

age_earnings_dict = {}


def build_age_earning_dict(age_index, earnings):
    if age_index not in age_earnings_dict.keys():
        age_earnings_dict[age_index] = [0, 0]
    if earnings > 1:
        age_earnings_dict[age_index] = [age_earnings_dict[age_index][0] + 1, age_earnings_dict[age_index][1] + earnings]


'''for idx, r in candidate_analysis_df.iterrows():
    if r["CONSTITUENCY"] in result_dict:
        if r["CANDIDATE_NAME"] in result_dict[r["CONSTITUENCY"]]:
            dictionary = result_dict[r["CONSTITUENCY"]][r["CANDIDATE_NAME"]]
            is_re_contesting = (1 if str(r["CANDIDATE_ID"]) in recontesting_candidates else 0)
            candidate_analysis_df.loc[idx, "RE_CONTEST"] = is_re_contesting
            candidate_analysis_df.loc[idx, "TOTAL_VOTES"] = dictionary["TOTAL_VOTES"]
            candidate_analysis_df.loc[idx, "VOTING_PERCENTAGE"] = dictionary["VOTING_PERCENTAGE"] * 0.01
            age_index = get_age_related_points(r["AGE"])
            build_age_earning_dict(age_index, r["EARNINGS"])
            candidate_analysis_df.loc[idx, "AGE_INDEX"] = age_index
            candidate_analysis_df.loc[idx, "PARTY_INDEX"] = get_value(party_df, [r["PARTY"], 'PARTY', 'INDEX', 0])
            candidate_analysis_df.loc[idx, "CONSTITUENCY_INDEX"] = get_value(constituency_df,[r["CONSTITUENCY"], 'CONSTITUENCY', 'INDEX', 0])
            candidate_analysis_df.loc[idx, "STATE_INDEX"] = get_value(state_df, [r["STATE"], 'STATE', 'INDEX', 0])
            result_count = result_count + 1
        else:
            nearest = difflib.get_close_matches(r["CANDIDATE_NAME"], result_dict[r["CONSTITUENCY"]].keys(), n=1, cutoff=0.3)
            if len(nearest) > 0:
                dictionary = result_dict[r["CONSTITUENCY"]][nearest[0]]
                is_re_contesting = (1 if r["CANDIDATE_ID"] in recontesting_candidates else 0)
                candidate_analysis_df.loc[idx, "RE_CONTEST"] = is_re_contesting
                candidate_analysis_df.loc[idx, "TOTAL_VOTES"] = dictionary["TOTAL_VOTES"]
                candidate_analysis_df.loc[idx, "VOTING_PERCENTAGE"] = dictionary["VOTING_PERCENTAGE"] * 0.01
                age_index = get_age_related_points(r["AGE"])
                build_age_earning_dict(age_index, r["EARNINGS"])
                candidate_analysis_df.loc[idx, "AGE_INDEX"] = age_index
                candidate_analysis_df.loc[idx, "PARTY_INDEX"] = get_value(party_df, [r["PARTY"], 'PARTY', 'INDEX', 0])
                candidate_analysis_df.loc[idx, "CONSTITUENCY_INDEX"] = get_value(constituency_df,[r["CONSTITUENCY"], 'CONSTITUENCY', 'INDEX', 0])
                candidate_analysis_df.loc[idx, "STATE_INDEX"] = get_value(state_df, [r["STATE"], 'STATE', 'INDEX', 0])
                closest_count = closest_count + 1

'''
result_count = 0

for idx, r in candidate_analysis_df.iterrows():

    if r["AGE_INDEX"] in age_earnings_dict:
        [t, s] = age_earnings_dict[r["AGE_INDEX"]]
        earnings_average = round(s/t, 2)
        if  r["EARNINGS"] <= (earnings_average + (earnings_average * 0.5)) and r["EARNINGS"] >= (earnings_average - (earnings_average * 0.5)):
            candidate_analysis_df.loc[idx, "EARNINGS_POINTS"] = 1
        else:
            candidate_analysis_df.loc[idx, "EARNINGS_POINTS"] = 0

    if pd.isna(r["MEDIA_POPULARITY"]):
        candidate_analysis_df.loc[idx, "MEDIA_POPULARITY"] = get_media_popularity_score(r["CANDIDATE_NAME"])

    result_count = result_count + 1

    if result_count % 100 == 0:

        logging.info("Saving for %d records" % result_count)

        candidate_analysis_df.to_csv(cfg["OUTPUT_DATA_SRC"]['CANDIDATE_ANALYSED_LIST']['CSV'], index=False,
                                     header=True)
        candidate_analysis_df.to_json(cfg["OUTPUT_DATA_SRC"]['CANDIDATE_ANALYSED_LIST']['JSON'],
                                      orient='records')


candidate_analysis_df.to_csv(cfg["OUTPUT_DATA_SRC"]['CANDIDATE_ANALYSED_LIST']['CSV'], index=False, header=True)
candidate_analysis_df.to_json(cfg["OUTPUT_DATA_SRC"]['CANDIDATE_ANALYSED_LIST']['JSON'], orient='records')

logging.info("%d candidate details found and %d closest once" % (result_count, closest_count))




