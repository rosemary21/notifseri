package com.creditville.notifications.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CardUtil {

    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
    public JSONObject getJsonObjResponse(String strResp) throws ParseException {
        JSONObject respObj = null;
        if(null != strResp && !StringUtils.isEmpty(strResp)){
            if(strResp.charAt(0) == '['){
//                System.out.println("response  is an array");
                JSONArray loanRespArr = (JSONArray) new JSONParser().parse(strResp);
                for(int i=0; i < loanRespArr.size(); i++){

                    respObj = (JSONObject) loanRespArr.get(i);
//                    System.out.println("respObj is: "+respObj);
                }
            }else if(strResp.charAt(0) == '{'){
                respObj = (JSONObject) new JSONParser().parse(strResp);
//                System.out.println("final respObj: "+respObj);
            }
        }
//        System.out.println("respObj: "+respObj);
        return respObj;
    }

    public String checkNullStr(String str){
        if(null != str)
            return str;
        else
          return "N/A";
    }

    public String checkNullStr(Object str){
        if(null != str)
            return (String) str;
        else
          return "N/A";
    }

    public String getObjectString(String param){
        if(param.charAt(6) == '(') {
            param = param.substring(7, param.length()-1);
            //Remove qutation marks
            param.replaceAll("\"", "");
            System.out.println("ENTRY getObjectString -> param: "+param);
            return param;
        }
        else return param;
    }
}
