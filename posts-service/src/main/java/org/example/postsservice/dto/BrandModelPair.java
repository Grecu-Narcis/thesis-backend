package org.example.postsservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BrandModelPair {
    private String brand;
    private List<String> models;
}

