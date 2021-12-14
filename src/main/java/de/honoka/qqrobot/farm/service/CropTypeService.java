package de.honoka.qqrobot.farm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import de.honoka.qqrobot.farm.common.Season;
import de.honoka.qqrobot.farm.database.dao.CropTypeDao;
import de.honoka.qqrobot.farm.database.service.CropTypeDbService;
import de.honoka.qqrobot.farm.entity.farm.CropType;
import de.honoka.qqrobot.spring.boot.starter.component.ExceptionReporter;
import de.honoka.util.file.FileUtils;
import de.honoka.util.text.CsvTable;
import de.honoka.util.various.ListRunner;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CropTypeService {

    public Map<String, CropType> getNameTypeMap(List<String> typeNames) {
        Map<String, CropType> result = new HashMap<>();
        for(String typeName : typeNames) {
            if(result.containsKey(typeName)) continue;
            result.put(typeName, cropTypeDao.selectById(typeName));
        }
        return result;
    }

    public List<CropType> randomType(String landform, Integer shopVolume) {
        List<CropType> cropTypeList = new ArrayList<>();
        Random ra = new Random();
        List<CropType> allTypes = cropTypeDao.selectList(new QueryWrapper<>(
                new CropType().setLandform(landform)));
        //当季作物
        int thisSeasonTypeNum = shopVolume / 3;
        String thisSeason = Season.getSeason(new Date());
        List<CropType> thisSeasonTypes = allTypes.stream().filter(
                type -> type.getSeason().equals(thisSeason)
        ).collect(Collectors.toList());
        for(int i = 0; i < thisSeasonTypeNum; i++) {
            cropTypeList.add(thisSeasonTypes.get(
                    ra.nextInt(thisSeasonTypes.size())
            ));
        }
        //所有季节作物
        for(int i = 0; i < shopVolume - thisSeasonTypeNum; i++) {
            cropTypeList.add(allTypes.get(ra.nextInt(allTypes.size())));
        }
        return cropTypeList;
    }

    /**
     * 将存储在文件中的作物列表更新到数据库中
     */
    @Transactional
    public void loadCropTypeInDatabase() {
        try {
            //加载作物列表
            String tablePath = FileUtils.getClasspath() + File.separator +
                    "farm" + File.separator + "crop_types.csv";
            File tableFile = new File(tablePath);
            //表格文件不存在，则不更新数据库表格
            if(!tableFile.exists()) return;
            CsvTable cropTypeTable = new CsvTable(tableFile);
            List<CropType> cropTypeList = new ArrayList<>();
            for(Map<String, String> row : cropTypeTable.rows) {
                CropType cropType = new CropType()
                        .setName(row.get("名称"))
                        .setLandform(row.get("地形"))
                        .setSeason(row.get("季节"))
                        .setSeedPrice(Integer.parseInt(row.get("种子售价")))
                        .setFruitPrice(Integer.parseInt(row.get("果实售价")));
                cropTypeList.add(cropType);
            }
            cropTypeDbService.saveOrUpdateBatch(cropTypeList);
        } catch(Exception e) {
            reporter.sendExceptionToDevelopingGroup(e);
        }
    }

    public CropTypeService(ListRunner initer) {
        initer.add(this::loadCropTypeInDatabase, 2);
    }

    @Resource
    private CropTypeDbService cropTypeDbService;

    @Resource
    private CropTypeDao cropTypeDao;

    @Resource
    private ExceptionReporter reporter;
}
