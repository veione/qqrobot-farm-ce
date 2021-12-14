package de.honoka.qqrobot.farm.database.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import de.honoka.qqrobot.farm.database.dao.CropTypeDao;
import de.honoka.qqrobot.farm.entity.farm.CropType;
import org.springframework.stereotype.Service;

@Service
public class CropTypeDbService extends ServiceImpl<CropTypeDao, CropType> {

}
