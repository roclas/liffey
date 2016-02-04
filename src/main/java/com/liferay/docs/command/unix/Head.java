package com.liferay.docs.command.unix;

import org.osgi.service.component.annotations.Component;
import org.unix4j.Unix4j;
import org.unix4j.unix.head.HeadOptions;

import java.util.List;
import java.util.Map;

/**
 * Created by carlos on 04/02/16.
 */
@Component(
    immediate = true,
    property = {
        "osgi.command.scope=liffey",
        "osgi.command.function=head"
    },
    service = Object.class
)
public class Head extends AbstractCommand{
    public Object head(String ... args) {
        if(args.length>0){
            Map<String, List<String>> params = getParams(args);
            if(params.get("n")!=null){
                try{
                    return Unix4j.fromString(readStdIn()).head(Long.parseLong(params.get("n").get(0))).toStringResult();
                }catch(Exception e){ }//if something goes wrong we will ignore the input parameters
            }
        }
        return Unix4j.fromString(readStdIn()).head().toStringResult();
    }

}
