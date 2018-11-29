package com.xu.drools.rule.complexProblem;

import com.xu.drools.bean.Person;
import com.xu.drools.bean.XiaoMing;
import com.xu.drools.service.RulesService;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

/**
 * 使用kmodule的方式调用drools
 * /resources/META-INF/kmodule.xml
 * 小明喝水问题
 */
public class MingDrink {

    /**
     * 小明喝汽水问题
     *
     * 1元钱一瓶汽水，喝完后两个空瓶换一瓶汽水，问：小明有20元钱，最多可以喝到几瓶汽水？
     */
    public static void main(final String[] args) {
        //KieContainer kc = KieServices.Factory.get().getKieClasspathContainer();
//        RulesService rulesService = new RulesService();
//        rulesService.initKieContainer();
//        KieSession kieSession = rulesService.newSession("src/main/resources/com/xu/drools/rule");
//        execute(kieSession);
//        rulesService.reloadRules(3);
//        KieSession db = rulesService.newSession("db");
//        Person person = new Person();
//        person.setAge(23);
//        person.setDesc("nmb");
//        person.setName("wangqiang");
//        db.insert(person);
//        db.fireAllRules();
//        db.dispose();
    }

    private static void execute(KieSession ksession) {
        XiaoMing xiaoMing=new XiaoMing();
        xiaoMing.setMoney(50);
        ksession.insert(xiaoMing);
        ksession.fireAllRules();
        ksession.dispose();
    }
}
