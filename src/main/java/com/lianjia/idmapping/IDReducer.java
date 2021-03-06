package com.lianjia.idmapping;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class IDReducer extends Reducer<Text, Text, Text ,Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,InterruptedException{


        Map<String,MapWritable> map = new HashMap();

        ArrayList<MapWritable> lj_device_list = new ArrayList<MapWritable>();

        //遍历迭代器
        for(Text value : values){
            String line = value.toString();
            String[] lines = line.split(",");

            MapWritable lj_device_map = new MapWritable();

            for(String lj_device_id : lines) {
                lj_device_map.put(new Text(lj_device_id), new LongWritable(1));
            }

            map.put(line, lj_device_map);

            //如果lj_device_id已存在列表中，就插入其中有交叉的Map中，缩短列表长度，否则，添加到列表中
            int is_contain = 0; //初始不包含
            //遍历列表，判断是否已包含lj_device_id
            for(int i = 0; i < lj_device_list.size(); i++){
                MapWritable lj_map = lj_device_list.get(i);
                for(Writable lj_key : lj_device_map.keySet()){
                    if(lj_map.containsKey(lj_key)){
                        lj_device_list.get(i).putAll(lj_device_map);
                        is_contain = 1;
                        break;
                    }
                }
                if(is_contain == 1){
                    break;
                }
            }
            if(is_contain == 0){
                lj_device_list.add(lj_device_map);
            }
        }

        // 归一
        for(int i = 0; i < lj_device_list.size(); i++){
            MapWritable lj_map = lj_device_list.get(i);
            for(int j = i+1; j < lj_device_list.size(); j++){
                for(Writable k : lj_map.keySet()){
                    if(lj_device_list.get(j).containsKey(k)){
                        lj_device_list.get(i).putAll(lj_device_list.get(j));
                        lj_device_list.remove(j);
                        j--;
                        break;
                    }
                }
            }
        }


        for(String lj_device_key : map.keySet()){
            MapWritable lj_device_map = map.get(lj_device_key);
            MapWritable lj_device_merge = null;
            int is_contain = 0;
            for(MapWritable lj_merge_key : lj_device_list){
                for(Writable lj_key : lj_device_map.keySet()){
                    if(lj_merge_key.containsKey(lj_key)){
                        is_contain = 1;
                        lj_device_merge = lj_merge_key;
                        break;
                    }
                }
                if(is_contain == 1){
                    break;
                }
            }

            context.write(new Text(lj_device_key),new Text(lj_device_merge.keySet().toString()));
        }

    }


}
