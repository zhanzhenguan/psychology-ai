package com.zzg.psychologyai.application.mapper;

import com.zzg.psychologyai.application.view.EmotionDiaryView;
import com.zzg.psychologyai.domain.model.EmotionDiary;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author: zzg
 * @date: 2026年10月19日  00:55
 * @version: 1.0
 */

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmotionDiaryMaps {


    EmotionDiaryView toDTO(EmotionDiary po);


    EmotionDiary toPO(EmotionDiaryView dto);


//    @Mappings({
//            @Mapping(target = "createdAt", source = "createTime"),
//            @Mapping(target = "updatedAt", source = "updateTime"),
//    })
//    EmotionDiaryView toView(EmotionDiaryRead read);

}
