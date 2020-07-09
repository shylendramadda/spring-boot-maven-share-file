package com.techatcore.sharefile.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shylendra Madda
 */
@Configuration
public class ModelDataMapper {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
