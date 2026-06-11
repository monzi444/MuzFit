package com.example.muzfit.service.dto.openfoodfacts;

import java.util.List;

public class SearchALiciousSearchResponseDto {

    private List<OpenFoodFactsProductDto> hits;
    private List<SearchALiciousErrorDto> errors;

    public List<OpenFoodFactsProductDto> getHits() {
        return hits;
    }

    public List<SearchALiciousErrorDto> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
