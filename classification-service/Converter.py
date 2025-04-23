import json
import re


class Converter:
    def __init__(self):
        with open('clear_data.json', 'r') as file:
            data = json.load(file)

        brand_to_models = {}

        for item in data:
            brand_to_models[item['brand']] = item['models']

        self.brand_to_models = brand_to_models

    def convert_to_brand_model(self, prediction: str):
        prediction = Converter.remove_car_type(prediction)

        brand = self.get_brand(prediction)
        year = prediction[-4:]
        prediction = prediction.lower().replace(brand.lower(), "")[:-4].strip()
        model = self.get_model(prediction, brand)

        return brand, model, year

    def get_brand(self, prediction: str):
        best_brand_match = None

        for brand in self.brand_to_models.keys():
            if brand.lower() == prediction.split()[0].lower():
                return brand

            if brand.lower() in prediction.lower():
                if best_brand_match is None:
                    best_brand_match = brand

                elif len(best_brand_match) < len(brand):
                    best_brand_match = brand

        return best_brand_match

    def get_model(self, prediction: str, brand: str):
        best_model_match = None

        for model in self.brand_to_models[brand]:
            if model.lower() in prediction.lower():
                if best_model_match is None:
                    best_model_match = model

                elif len(best_model_match) < len(model):
                    best_model_match = model

        return best_model_match

    @staticmethod
    def remove_car_type(prediction: str):
        car_types = [
            "sedan", "convertible", "hatchback", "SUV", "coupe", "wagon", "pickup",
            "minivan", "crossover", "roadster", "compact", "subcompact", "limousine",
            "cabriolet", "four-door", "two-door", "off-road vehicle", "coupe utility",
            "grand tourer", "microcar"
        ]

        # Create a regular expression pattern to match any car type
        pattern = r"\b(" + "|".join(car_types) + r")\b"

        result = re.sub(pattern, "", prediction, flags=re.IGNORECASE)

        # Remove extra spaces after replacement
        result = re.sub(r"\s+", " ", result).strip()

        return result

    def get_all_brands(self):
        return sorted(list(self.brand_to_models.keys()))

    def get_all_models(self, brand: str):
        return sorted(self.brand_to_models[brand])
