package com.xu.drools.rule.group;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

public class group {

    public static void main(final String[] args) {
        KieContainer kc = KieServices.Factory.get().getKieClasspathContainer();
        //System.out.println(kc.verify().getMessages().toString());
        execute(kc);
    }

    private static void execute(KieContainer kc) {
        KieSession ksession = kc.newKieSession("groupKS");
        // 执行分组 1
        ksession.getAgenda().getAgendaGroup("group2").setFocus();
        ksession.fireAllRules();

// 执行分组 2
        ksession.fireAllRules();

// 执行分组 3
        ksession.fireAllRules();
    }
}
