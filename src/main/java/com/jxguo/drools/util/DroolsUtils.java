package com.xu.drools.util;

import com.xu.drools.bean.Rules;
import com.xu.drools.dao.RulesDao;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DroolsUtils {
    /**
     * 默认规则文件所在上级路径
     */

    public static final String ROOT_RULES_PATH = "src/main/resources/com/mljr/bgrk/drools/rule";


    private static KieServices kieServices;

    private static KieRepository repository;

    private static ConcurrentHashMap<String, KieContainer> kieContainers = new ConcurrentHashMap<String, KieContainer>();

    private final static String GROUP_NAME = "MLJR-BGRK";

    private final static String VERSION = "1.0.0";

    static {
        initKieContainer();
    }


    /**
     * 获取规定目录下的规则文件
     *
     * @return
     */
    private static List<File> getRuleFiles(String filePath) {
        List<File> list = new ArrayList<File>();
        File rootDir = new File(ROOT_RULES_PATH + File.separator + filePath);
        if (rootDir.isDirectory()) {
            for (File f : rootDir.listFiles()) {
                if (f.getName().endsWith(".drl")) {
                    list.add(f);
                }
            }
        }
        return list;
    }


    /**
     * 获取默认目录下的规则文件
     *
     * @return
     */
    private static void initAllRule() {
        File rootDir = new File(ROOT_RULES_PATH);
        File[] files = rootDir.listFiles();
        for (File itemFile : files) {
            if (itemFile.isDirectory() ) {
                loadRules(itemFile.getName(),
                        getRulesFromLocalFile(itemFile.getName()));
            }
        }
    }

    private static List<ResourceWrapper> getRulesFromLocalFile(String path) {
        List<ResourceWrapper> resourceWrappers = new ArrayList<ResourceWrapper>();
        List<File> ruleFiles = new ArrayList<File>();
        try {
            ruleFiles =  getRuleFiles(path);

        } catch (Exception e) {}
        if(ruleFiles.isEmpty()){
            throw new RuntimeException("can't load rules from " + path );
        }

        for (File file : ruleFiles) {

            resourceWrappers.add(new ResourceWrapper(ResourceFactory.newFileResource(file),
                    DroolsUtils.ROOT_RULES_PATH + File.separator  + file.getName()));
        }
        return resourceWrappers;
    }

    private static List<ResourceWrapper> getRulesFromDB(Integer id) {
        RulesDao rulesDao = (RulesDao) SpringContextUtils.getBean("rulesDao");
        List<ResourceWrapper> resourceWrappers = new ArrayList<ResourceWrapper>();
        List<Rules> ruleList;
        if (id == null) {
           ruleList = rulesDao.getRuleList();
        }else {
            ruleList = new ArrayList<>(1);
            ruleList.add(rulesDao.getById(id));
        }
        for (Rules rules : ruleList) {

            resourceWrappers.add(new ResourceWrapper(ResourceFactory.newByteArrayResource(rules.getRule().getBytes()), rules.getName()));
        }
        return resourceWrappers;
    }




    private static InternalKieModule createKieJar(KieServices ks, ReleaseId releaseId, List<ResourceWrapper> resourceWrappers) {
        KieFileSystem kfs = createKieFileSystemWithKProject(ks, true);
        kfs.writePomXML(getPom(releaseId));
        for (ResourceWrapper rw :resourceWrappers)
        kfs.write( rw.getTargetResourceName(), rw.getResource());
        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        if (!kieBuilder.getResults().getMessages().isEmpty()) {
            throw new IllegalStateException("Error creating KieBuilder." +kieBuilder.getResults().getMessages());
        }
        return (InternalKieModule) kieBuilder.getKieModule();
    }

    /**
     * 创建默认的kbase和stateful的kiesession
     *
     * @param ks
     * @param isdefault
     * @return
     */
    private static KieFileSystem createKieFileSystemWithKProject(KieServices ks, boolean isdefault) {
        KieModuleModel kproj = ks.newKieModuleModel();
        KieBaseModel kieBaseModel1 = kproj.newKieBaseModel("KBase").setDefault(isdefault)
                .setEqualsBehavior(EqualityBehaviorOption.EQUALITY).setEventProcessingMode(EventProcessingOption.STREAM);
        // Configure the KieSession.
        kieBaseModel1.newKieSessionModel("KSession").setDefault(isdefault)
                .setType(KieSessionModel.KieSessionType.STATEFUL);
        KieFileSystem kfs = ks.newKieFileSystem();
        String str = kproj.toXML();
        kfs.writeKModuleXML(kproj.toXML());
        return kfs;
    }


    private static void initKieContainer(){
        kieServices = KieServices.Factory.get();
        repository = kieServices.getRepository();
        initAllRule();
    }

    private static void loadRules(String rulePath, List<ResourceWrapper> resourceWrappers) {
        // if failed throws Exception
        ReleaseId releaseId = kieServices.newReleaseId(GROUP_NAME, rulePath, VERSION);
        InternalKieModule kieModule = DroolsUtils.createKieJar(kieServices, releaseId, resourceWrappers);
        // if succeed will add new module
        repository.addKieModule(kieModule);
        KieContainer newkieContainer = kieServices.newKieContainer(releaseId);
        if (kieContainers.get(rulePath) != null){
            KieContainer check = kieContainers.putIfAbsent(rulePath, newkieContainer);
            if (check != null){
                newkieContainer.dispose();
            }
        }
        kieContainers.put(rulePath, newkieContainer);

        newkieContainer.updateToVersion(releaseId);
    }

    public static void reloadRules(String rulePath){
        List<ResourceWrapper> rulesFromLocalFile = getRulesFromLocalFile(rulePath);
        loadRules(rulePath, rulesFromLocalFile);
    }

    public static void reloadRules(Integer id){
        List<ResourceWrapper> rulesFromDB = getRulesFromDB(id);
        loadRules("db", rulesFromDB);
    }

    public static KieSession newSession(String rulePath) {
        KieContainer kieContainer = kieContainers.get(rulePath);
        if(kieContainer==null){
            throw new RuntimeException("can't get KieContainer with the name:" + rulePath);
        }
        KieSession session = kieContainer.newKieSession();
        // 默认配置
        ApplicationContext applicationContext = SpringContextUtils.getApplicationContext();
        Object rulesDao = applicationContext.getBean("rulesDao");
        session.setGlobal("ApplicationContext", applicationContext);
        return session;
    }

    public static void fireRules(String rulePath, Object... facts) {
        KieSession session = null;
        try {
            session = newSession(rulePath);
            // add fact
            for (Object fact : facts) {
                session.insert(fact);
            }

            session.fireAllRules();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }

    public static void logRulesInKieBase(KieBase kieBase) {
        Collection<KiePackage> kiePackages = kieBase.getKiePackages();
        for (KiePackage nextKiePackage:kiePackages) {
            Collection<Rule> rules = nextKiePackage.getRules();
            for (Rule nextRule:rules) {
                System.out.println("Rule: " + nextRule.getPackageName() + "-" + nextRule.getName());
            }
        }
    }

    /**
     * 创建kjar的pom
     *
     * @param releaseId
     * @param dependencies
     * @return
     */
    private static String getPom(ReleaseId releaseId, ReleaseId... dependencies) {
        String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n"
                + "  <modelVersion>4.0.0</modelVersion>\n" + "\n" + "  <groupId>" + releaseId.getGroupId()
                + "</groupId>\n" + "  <artifactId>" + releaseId.getArtifactId() + "</artifactId>\n" + "  <version>"
                + releaseId.getVersion() + "</version>\n" + "\n";
        if (dependencies != null && dependencies.length > 0) {
            pom += "<dependencies>\n";
            for (ReleaseId dep : dependencies) {
                pom += "<dependency>\n";
                pom += "  <groupId>" + dep.getGroupId() + "</groupId>\n";
                pom += "  <artifactId>" + dep.getArtifactId() + "</artifactId>\n";
                pom += "  <version>" + dep.getVersion() + "</version>\n";
                pom += "</dependency>\n";
            }
            pom += "</dependencies>\n";
        }
        pom += "</project>";
        return pom;
    }


    public static class ResourceWrapper {
        private Resource resource;

        private String   targetResourceName;

        public ResourceWrapper(Resource resource, String targetResourceName) {
            this.resource = resource;
            this.targetResourceName = targetResourceName;
        }

        public Resource getResource() {
            return resource;
        }

        public String getTargetResourceName() {
            return targetResourceName;
        }

        public void setResource(Resource resource) {
            this.resource = resource;
        }

        public void setTargetResourceName(String targetResourceName) {
            this.targetResourceName = targetResourceName;
        }
    }
}
