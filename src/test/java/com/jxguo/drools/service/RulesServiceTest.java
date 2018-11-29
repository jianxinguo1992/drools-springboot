package com.xu.drools.service;

import com.xu.drools.DroolsSpringbootApplicationTests;
import com.xu.drools.bean.Person;
import com.xu.drools.util.DroolsUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


public class RulesServiceTest extends DroolsSpringbootApplicationTests {

    private static Logger logger = LogManager.getLogger();
    @Autowired
    RulesService rulesService;

    @Autowired
    ThreadPoolTaskExecutor kafkaThreadPoolA;

    @Autowired
    RetryService retryService;

    @Test
    public void payTest() throws Exception {
        int store = retryService.minGoodsnum(-1);
        System.out.println("库存为：" + store);
    }

    @Test
    public void getKieSession() {

        kafkaThreadPoolA.execute(() ->{
            System.out.println(Thread.currentThread().getId());
            logger.info(Thread.currentThread().getId());
        });
        kafkaThreadPoolA.execute(() ->{
            System.out.println(Thread.currentThread().getId());
            logger.info(Thread.currentThread().getId());
        });
        kafkaThreadPoolA.execute(() ->{
            System.out.println(Thread.currentThread().getId());
            logger.info(Thread.currentThread().getId());
        });
        kafkaThreadPoolA.execute(() ->{
            System.out.println(Thread.currentThread().getId());
            logger.info(Thread.currentThread().getId());
        });
        kafkaThreadPoolA.execute(() ->{
            System.out.println(Thread.currentThread().getId());
            logger.info(Thread.currentThread().getId());
        });

    }

    @Test
    public void getRulesWrite() {

        Person p1 = new Person(35, "xu", "handsome");
        Person p2 = new Person(39, "hua", "handsome");
        KieSession kieSession = DroolsUtils.newSession("rule2");
        DroolsUtils.logRulesInKieBase(kieSession.getKieBase());
        DroolsUtils.logRulesInKieBase(DroolsUtils.newSession("complexProblem").getKieBase());
        DroolsUtils.fireRules("rule2", p1, p2);

        DroolsUtils.reloadRules("rule2");
        System.out.println();
    }


}