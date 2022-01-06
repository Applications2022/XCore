package de.ruben.xcore.gamble.model;

import de.ruben.xdevapi.XDevApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GambleParticipant {
    private UUID uuid;
    private String name;
    private double bet;
    private GambleState gambleState;

    public String getBetDisplay(){
        return "§7"+name+" §8("+getBetString()+"§8)";
    }

    public String getBetString(){
        if(gambleState == GambleState.UNKNOWN){
            return "§b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(bet)+"€";
        }else if(gambleState == GambleState.LOST){
            return "§c-"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(bet)+"€";
        }else{
            return "§2+"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(bet)+"€";
        }
    }
}
