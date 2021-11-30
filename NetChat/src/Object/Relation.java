package Object;
/**
 * author：李志强
 * class：网络194
 * date：2021-11-24
 * 注：本项目github：https://github.com/AxisZql/NetProgramExp-
 */

public class Relation {

    private final Integer type;//关系类型
    private final Integer ua_id;
    private final Integer ub_id;

    public Relation( Integer type, Integer ua_id, Integer ub_id) {
        this.type = type;
        this.ua_id = ua_id;
        this.ub_id = ub_id;
    }


    public Integer getUa_id() {
        return ua_id;
    }

    public Integer getUb_id() {
        return ub_id;
    }

    public Integer getType() {
        return type;
    }

}
