package com.example.dogmiddleware.dto;

import java.util.List;
import java.util.Map;

public record BreedListData(Map<String, List<String>> breeds) {
}
