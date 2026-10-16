package com.example.backend.insight.mapper;

import com.example.backend.insight.dto.InsightResponseDTO;
import com.example.backend.insight.entity.Insight;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface InsightMapper {
    InsightResponseDTO toResponseDTO(Insight entity);
    List<InsightResponseDTO> toResponseDTOList(List<Insight> insights);
}
