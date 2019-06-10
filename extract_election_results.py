import logging
import codecs
import json
import pandas as pd


# Loads json configuration from the configuration file.
def get_config(conf_path):
    with codecs.open(conf_path, 'r', 'utf-8-sig') as json_data:
        d = json.load(json_data)
        return d


def create_df(file_name):
    return pd.read_csv(file_name, header='infer')


logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)

cfg = get_config("config/cfg.json")


candidate_analysis_df = create_df(cfg["OUTPUT_DATA_SRC"]["CANDIDATE_ANALYSED_LIST"]['CSV'])

voting_results_df = create_df(cfg["OUTPUT_DATA_SRC"]["VOTING_RESULTS"]['CSV'])

result_dict = {}

for index, row in voting_results_df.iterrows():
    if row["CONSTITUENCY_NAME"] not in result_dict:
        result_dict[row["CONSTITUENCY_NAME"]] = {}
    result_dict[row["CONSTITUENCY_NAME"]][row["CANDIDATE_NAME"]] = row

result_count = 0

for index, row in candidate_analysis_df.iterrows():
    if row["CONSTITUENCY"] in result_dict:
        if row["CANDIDATE_NAME"] in result_dict[row["CONSTITUENCY"]]:
            dictionary = result_dict[row["CONSTITUENCY"]][row["CANDIDATE_NAME"]]
            candidate_analysis_df.loc[index, "TOTAL_VOTES"] = dictionary["TOTAL_VOTES"]
            candidate_analysis_df.loc[index, "VOTING_PERCENTAGE"] = dictionary["VOTING_PERCENTAGE"] * 0.01
            result_count = result_count + 1


candidate_analysis_df.to_csv(cfg["OUTPUT_DATA_SRC"]['CANDIDATE_ANALYSED_LIST']['CSV'], index=False, header=True)
candidate_analysis_df.to_json(cfg["OUTPUT_DATA_SRC"]['CANDIDATE_ANALYSED_LIST']['JSON'], orient='records')

logging.info("%d candidate details found" % result_count)




