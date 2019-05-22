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


def execute(run, csv_file, json_file):
    logging.info("Building '%s' file" % csv_file)
    df = run()
    df.to_csv(csv_file, index=False, header=True)
    df.to_json(json_file, orient='records')
    logging.info("Exported %d rows to '%s' file" % (len(df), csv_file))


def create_df(file_name):
    return pd.read_csv(file_name, header='infer')


def get_value(df, t):
    for index, row in df.iterrows():
        if row[t[1]] == t[0]:
            return row[t[2]]
    return t[3]


logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)

cfg = get_config("config/cfg.json")
DATA_SET_URL = cfg["DATA_SET_URL"]
CANDIDATE_DS_URL = cfg["CANDIDATE_DS_URL"]
INPUT_DATA_SRC = cfg["INPUT_DATA_SRC"]
OUTPUT_DATA_SRC = cfg["OUTPUT_DATA_SRC"]
[candidates_data_df, education_idx_df, weights_df] = [create_df(INPUT_DATA_SRC["CANDIDATES_LIST"]),
                                                      create_df(INPUT_DATA_SRC["EDUCATION_INDEX"]),
                                                      create_df(INPUT_DATA_SRC["WEIGHTAGE"])]


''' Utility Class with few utility methods '''


class ElectionUtils(object):

    def get_earnings_points(self, age, earnings, age_idx_df):
        for index, row in age_idx_df.iterrows():
            if row['FROM'] <= age and row['TO'] >= age:
                if row['MAXIMUM_EARNINGS'] < earnings or row['MINIMUM_EARNINGS'] > earnings:
                    return 0
                else:
                    return 1

    def get_criminal_case_points(self, pending_cases, convicted_cases):
        # If convicted we get a negative score twice that of offenses one has committed
        if convicted_cases > 0:
            return -2 * convicted_cases
        # we get a negative score depending on no. of offenses committed
        elif pending_cases > 0:
            return -1 * pending_cases
        else:
            return 0

    def get_age_df(self):
        return self.age_earning_df(create_df(INPUT_DATA_SRC["AGE_INDEX"]), candidates_data_df)

    '''Returns points which can be earned given the age bracket 
       (Please note older people score less through this system)'''
    def get_age_related_points(self, age, age_df):
        for index, row in age_df.iterrows():
            if row['FROM'] <= age and row['TO'] >= age:
                return row['POINTS']
        return 1

    def get_weight(self, key):
        return self.weights_df[self.weights_df['KEY'] == key]['WEIGHT'].values[0]

    def get_edu_from_points(self, points):
        for index, row in education_idx_df.iterrows():
            if row['POINTS'] == int(points):
                return row['EDUCATION']
        return 'Literate'

    ''' Returns points calculated based on education '''
    def get_edu_points(self, education):
        return education_idx_df[education_idx_df['EDUCATION'] == education]['POINTS'].values[0]

    def age_earning_df(self, age_idx_df, candidates_data_df):
        for index, row in age_idx_df.iterrows():
            cd_df = candidates_data_df[
                (candidates_data_df['AGE'] >= row['FROM']) & (candidates_data_df['AGE'] <= row['TO'])]
            # Earnings calculated by the formula EARNINGS = (MOVABLE ASSETS + IMMOVABLE ASSETS) - LIABLITIES
            mean_earnings = ((cd_df['MOVABLE_ASSETS'] + cd_df['IMMOVABLE_ASSETS']) - cd_df['LIABLITIES']).mean()
            # Calculating mean earnings
            age_idx_df.loc[index, 'AVERAGE_EARNINGS'] = mean_earnings
            # Taking boundaries of 50% average for earnings evaluation
            age_idx_df.loc[index, 'MINIMUM_EARNINGS'] = mean_earnings - (mean_earnings * 0.5)
            age_idx_df.loc[index, 'MAXIMUM_EARNINGS'] = mean_earnings + (mean_earnings * 0.5)
        return age_idx_df

    def get_candidate_data(self, candidate_id):
        response = requests.get(CANDIDATE_DS_URL, params={"candidate_id": candidate_id}, proxies=cfg['PROXY'])
        if response.status_code == 200:
            hp = BeautifulSoup(response.text, 'lxml')
            age_ele = 60
            try:
                age_ele = int(hp.findAll("div", {"class": "grid_2 alpha"})[2].get_text().strip().split(":")[1].strip())
            except ValueError:
                logging.error("Candidate : %s - Age is Unknown defaulting it to 60" % candidate_id)

            [assets, liabilities] = [
                self.get_value(hp.find_all("div", {"class": "bottom-border-div red fullWidth"})[0].find("b").get_text()
                               .strip().replace(",", ""), candidate_id),
                self.get_value(hp.find_all("div", {"class": "bottom-border-div blue fullWidth"})[0].find("b").get_text()
                               .strip().replace(",", ""), candidate_id)]

            logging.debug("Age : %d, Assets : %d & Liabilities : %d" % (age_ele, assets, liabilities))

            return [int(age_ele), assets, liabilities]
        else:
            return [60, 0.0, 0.0]

    def get_value(self, str, candidate_id):
        lst = str.split(" ")
        val = 0
        if len(lst) > 1:
            try:
                val = float(lst[1])
            except ValueError:
                logging.error("Candidate : %s - Value is Unknown defaulting it to 0" % candidate_id)
        return val

    def extract_data(self):

        logging.info("Extracting '%s' data" % OUTPUT_DATA_SRC['CANDIDATE_ANALYSED_LIST']['CSV'])

        cal_df = create_df(OUTPUT_DATA_SRC["CANDIDATE_ANALYSED_LIST"]['CSV'])
        age_idx_df = self.get_age_df()

        for index, row in cal_df.iterrows():
            cal_df.loc[index, "AGE_GROUP_IDX"] = self.get_age_related_points(row["AGE"], age_idx_df)
            cal_df.loc[index, "EDUCATION_GROUP_IDX"] = self.get_edu_points(row["EDUCATION"])

        cal_df.to_csv(OUTPUT_DATA_SRC['CANDIDATE_ANALYSED_LIST']['CSV'], index=False, header=True)

    def extract_candidate_data(self, url):

        party_df = create_df(INPUT_DATA_SRC['POLITICAL_PARTY_INDEX'])
        state_df = create_df(INPUT_DATA_SRC['STATES_INDEX'])
        state_constituency_df = create_df(INPUT_DATA_SRC['CONSTITUENCIES'])
        age_idx_df = self.get_age_df()

        logging.info("Extracting '%s' data" % OUTPUT_DATA_SRC['CANDIDATE_ANALYSED_LIST']['CSV'])

        response = requests.get(url, params=cfg['DATA_SET_PARAMS'], proxies=cfg['PROXY'])

        labels = ["CANDIDATE_ID", "CANDIDATE_NAME", "AGE", "CONSTITUENCY", "STATE", "PARTY",
                  "NO_PENDING_CRIMINAL_CASES", "EDUCATION", "EARNINGS", "STATE_LITERACY_RATE",
                  "STATE_SEAT_SHARE", "PARTY_GRP_IDX", "AGE_GROUP_IDX", "EDUCATION_GROUP_IDX",
                  "DELTA_STATE_VOTER_TURNOUT", "NO_OF_PHASES"]

        if response.status_code == 200:
            html_parser = BeautifulSoup(response.text, 'lxml')
            table = html_parser.find_all('table')[1]
            candidate_al_df = pd.DataFrame(columns=labels)
            row_marker = 0
            idx = 0
            for row in table.find_all('tr'):
                if row_marker > 1:
                    td = row.find_all('td')
                    candidate_id = td[1].find("a").get("href").split("=")[1]
                    state = get_value(state_constituency_df, [td[2].get_text().strip(), 'CONSTITUENCY', 'STATE', 'NA'])
                    party = td[3].get_text().strip()
                    [age, assets, liabilities] = self.get_candidate_data(candidate_id)
                    age_related_idx = self.get_age_related_points(age, age_idx_df)
                    state_literacy_rate = get_value(state_df, [state, 'STATE', 'LITERACY_RATE', 0.0])
                    state_seat_share = get_value(state_df, [state, 'STATE', 'SEAT_SHARE', 0.0])
                    delta_state_voter_turnout = round(
                        (get_value(state_df, [state, 'STATE', 'CURRENT_VOTER_TURNOUT', 0.0]) -
                         get_value(state_df, [state, 'STATE', 'PREVIOUS_VOTER_TURNOUT', 0.0]))
                        , 4)
                    state_no_phases = get_value(state_df, [state, 'STATE', 'NO_OF_PHASES', 1])
                    party_grp_idx = get_value(party_df, [party, 'PARTY', 'POINTS', 1])
                    edu_idx = self.get_edu_points(td[5].get_text().strip())
                    row_dict = {labels[0]: candidate_id,
                                labels[1]: td[1].find("a").get_text().strip().replace(",", ""),
                                labels[2]: age,
                                labels[3]: td[2].get_text().strip(),
                                labels[4]: state,
                                labels[5]: party,
                                labels[6]: td[4].get_text().strip(),
                                labels[7]: td[5].get_text().strip(),
                                labels[8]: round((assets - liabilities), 2),
                                labels[9]: round(state_literacy_rate, 4),
                                labels[10]: round(state_seat_share, 4),
                                labels[11]: int(party_grp_idx),
                                labels[12]: int(age_related_idx),
                                labels[13]: int(edu_idx),
                                labels[14]: delta_state_voter_turnout,
                                labels[15]: int(state_no_phases)}
                    candidate_al_df.loc[idx] = row_dict
                    idx += 1
                row_marker += 1
            candidate_al_df.to_csv(OUTPUT_DATA_SRC['CANDIDATE_ANALYSED_LIST']['CSV'], index=False, header=True)
            logging.info("Extracted '%s' data" % OUTPUT_DATA_SRC['CANDIDATE_ANALYSED_LIST']['CSV'])


'''Transformations object'''


class CandidateDataTransformation(object):

    # Initilization
    utils = ElectionUtils()

    utils.extract_candidate_data(DATA_SET_URL)

    candidate_analysis_df = create_df(OUTPUT_DATA_SRC["CANDIDATE_ANALYSED_LIST"]['CSV'])

    age_idx_df = utils.get_age_df()

    '''Returns points based on income tax compliance, if you are a regular payer you earn a point'''
    def get_tax_compliance_points(self, tax_compliance):
        dict = {"YES": 1, "NO": 0}
        return dict[tax_compliance]

    '''Returns points based on government dues payout compliance, if you are a regular payer you earn a point'''
    def get_govt_due_points(self, govt_due):
        dict = {"YES": 0, "NO": 1}
        return dict[govt_due]

    '''Returns points based on residency, if the contestent is a local he/she gets a point'''
    def get_local_residency_points(self, local_residency):
        dict = {"YES": 1, "NO": 0}
        return dict[local_residency]


    def calculate_party_criminal_score(self):
        ca_df = self.candidate_analysis_df.groupby(['PARTY']).agg(
            {'CANDIDATE_NAME': 'count', 'NO_PENDING_CRIMINAL_CASES': 'mean'}).reset_index().rename(
            columns={'CANDIDATE_NAME': 'NO_CONTESTING_CANDIDATES',
                     'NO_PENDING_CRIMINAL_CASES': 'PENDING_CRIMINAL_CASES_PER_CANDIDATE'})
        ca_df['PENDING_CRIMINAL_CASES_PER_CANDIDATE'] = round(ca_df['PENDING_CRIMINAL_CASES_PER_CANDIDATE'], 2)
        ca_df = ca_df.sort_values('PENDING_CRIMINAL_CASES_PER_CANDIDATE', ascending=False)
        return ca_df

    def calculate_state_criminal_score(self):
        ca_df = self.candidate_analysis_df.groupby(['STATE']).agg(
            {'CANDIDATE_NAME': 'count', 'NO_PENDING_CRIMINAL_CASES': 'mean'}).reset_index().rename(
            columns={'CANDIDATE_NAME': 'NO_CONTESTING_CANDIDATES',
                     'NO_PENDING_CRIMINAL_CASES': 'PENDING_CRIMINAL_CASES_PER_CANDIDATE'})
        ca_df['PENDING_CRIMINAL_CASES_PER_CANDIDATE'] = round(ca_df['PENDING_CRIMINAL_CASES_PER_CANDIDATE'], 2)
        ca_df = ca_df.sort_values('PENDING_CRIMINAL_CASES_PER_CANDIDATE', ascending=False)
        return ca_df

    def build_candidate_analysis_df(self, df):
        for index, row in df.iterrows():
            df.loc[index, 'POINTS_FOR_EDUCATION'] = self.utils.get_edu_points(row['EDUCATION'])
        return df

    def calculate_party_education_score(self):
        edu_df = self.build_candidate_analysis_df(self.candidate_analysis_df)
        edu_df = edu_df.groupby(['PARTY']).agg({'CANDIDATE_NAME': 'count', 'POINTS_FOR_EDUCATION': 'median'})\
            .reset_index().rename(columns={'CANDIDATE_NAME': 'NO_CONTESTING_CANDIDATES',
                                           'POINTS_FOR_EDUCATION': 'EDUCATION_INDEX'})
        edu_df['EDUCATION_INDEX'] = round(edu_df['EDUCATION_INDEX'])

        for index, row in edu_df.iterrows():
            edu_df.loc[index, 'STANDARD_EDUCATION_LEVEL'] = self.utils.get_edu_from_points(row['EDUCATION_INDEX'])
        edu_df = edu_df.sort_values('STANDARD_EDUCATION_LEVEL', ascending=False)
        return edu_df

    def calculate_state_education_score(self):
        edu_df = self.build_candidate_analysis_df(self.candidate_analysis_df)
        edu_df = edu_df.groupby(['STATE']).agg({'CANDIDATE_NAME': 'count', 'POINTS_FOR_EDUCATION': 'median'})\
            .reset_index().rename(columns={'CANDIDATE_NAME': 'NO_CONTESTING_CANDIDATES',
                                           'POINTS_FOR_EDUCATION': 'EDUCATION_INDEX'})
        edu_df['EDUCATION_INDEX'] = round(edu_df['EDUCATION_INDEX'])

        for index, row in edu_df.iterrows():
            edu_df.loc[index, 'STANDARD_EDUCATION_LEVEL'] = self.utils.get_edu_from_points(row['EDUCATION_INDEX'])
        edu_df = edu_df.sort_values('STANDARD_EDUCATION_LEVEL', ascending=False)
        return edu_df


# Main Section

cdt = CandidateDataTransformation()

# execute(cdt.evaluate, OUTPUT_DATA_SRC["CONTESTANT_LIST"]["CSV"], OUTPUT_DATA_SRC["CONTESTANT_LIST"]["JSON"])

# Aggregating analysis for party wise candidate recuirtment based on criminal history.
execute(cdt.calculate_party_criminal_score, OUTPUT_DATA_SRC["PENDING_CRIMINAL_CASES_BY_PARTY"]["CSV"],
        OUTPUT_DATA_SRC["PENDING_CRIMINAL_CASES_BY_PARTY"]["JSON"])

# Aggregating analysis for state wise candidate recuirtment based on criminal history.
execute(cdt.calculate_state_criminal_score, OUTPUT_DATA_SRC["PENDING_CRIMINAL_CASES_BY_STATE"]["CSV"],
        OUTPUT_DATA_SRC["PENDING_CRIMINAL_CASES_BY_STATE"]["JSON"])

# Aggregating analysis for party wise candidate recuirtment based on education.
execute(cdt.calculate_party_education_score, OUTPUT_DATA_SRC["EDUCATION_INDEX_BY_PARTY"]["CSV"],
        OUTPUT_DATA_SRC["EDUCATION_INDEX_BY_PARTY"]["JSON"])

# Aggregating analysis for state wise candidate recuirtment based on education.
execute(cdt.calculate_state_education_score, OUTPUT_DATA_SRC["EDUCATION_INDEX_BY_STATE"]["CSV"],
        OUTPUT_DATA_SRC["EDUCATION_INDEX_BY_STATE"]["JSON"])







