### 介绍
- Spring Batch是一个轻量级，全面的批处理框架，旨在开发对企业系统日常运营至关重要的强大批处理应用程序。Spring Batch不是一个调度框架。在商业和开源空间中都有许多优秀的企业调度器（例如Quartz，Tivoli，Control-M等）。它旨在与调度程序一起工作，而不是替换调度程序。
- Spring Batch让程序员开发批处理程序时，可以更加关注于业务逻辑。
- 注意：Spring Batch仅仅是一个用于开发批处理程序的**框架**

### 使用场景（可开发出那几种批处理程序）
- 典型批处理的基本流程：
    - 从数据库，文件或队列等输入源中读取大量记录。
    - 以某种方式处理数据。
    - 以修改的形式写回数据到输出源。
- spring batch 可以为典型批处理附加以下功能
    - 定期提交
    - 并发处理作业
    - 分阶段的企业消息驱动处理
    - 大规模并发
    - 失败后手动或自动计划重新启动
    - 顺序处理相关步骤
    - 部分处理：跳过某些记录


### 入门使用
- 批处理示例项目参考了[官方入门教程](https://spring.io/guides/gs/batch-processing/)
- 批处理示例项目的基本流程：
    - 这个例子选择从文件读取记录
    - 处理读取到的数据
    - 以修改的形式写回数据到mysql数据库的`people`表。
##### 输入
- 创建数据文件`src/main/resources/sample-data.csv`，并添加以下数据
```
Jill,Doe
Joe,Doe
Justin,Doe
Jane,Doe
John,Doe
```
##### 处理
- 创建一个处理器处理数据。
- 下面逻辑很简单，仅仅是把读取到的`person`数据的名称字母都转换为大写
```
public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
    public Person process(final Person person) throws Exception {
        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();

        final Person transformedPerson = new Person(firstName, lastName);

        log.info("Converting (" + person + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }

}
```

##### 输出
- 给输出源mysql数据库创建一个`people`表
```
CREATE TABLE people  (
    person_id BIGINT  NOT NULL PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);

```
- 创建一个对应于`people`表的实体对象`Person`
```
public class Person {
    private String lastName;
    private String firstName;
    //get&set.....
}
```

#### 整合上面3个流程
- 创建配置类`BatchConfiguration`，逻辑如下：
    - `reader()`方法配置输入
        - 主要配置输入文件`sample-data.csv`中的数据与实体类`Person`之间的映射关系
        - 用于spring batch自动把文件中的数据转换成`Person`对象
    - `processor()`方法配置处理器
    - `writer()`方法配置输出
        - 主要获取数据源与配置插入数据
    - `step1()`方法配置一个批处理步骤
        - 定义一个进行`输入，处理，输出`的批处理步骤。
    - 最终由`importUserJob()`方法创建一个批处理任务
        - 定义任务中要执行的步骤
```
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    // tag::readerwriterprocessor[]
    @Bean
    public FlatFileItemReader<Person> reader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("sample-data.csv"));
        FixedLengthTokenizer fixedLengthTokenizer = new FixedLengthTokenizer();
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[]{"firstName", "lastName"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});
        return reader;
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
        writer.setDataSource(dataSource);
        return writer;
    }
    
    @Bean
    public Job importUserJob(JobBuilderFactory jobBuilderFactory, Step step1) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JdbcBatchItemWriter<Person> writer) {
        return stepBuilderFactory.get("step1")
                .<Person, Person> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }
}
```

#### 运行结果
- 启动上面编写的批处理程序，可以看到输出如下
```
2018-07-02 15:27:06.989  INFO 16337 --- [           main] o.s.b.a.b.JobLauncherCommandLineRunner   : Running default command line with: []
2018-07-02 15:27:07.118  INFO 16337 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [SimpleJob: [name=importUserJob]] launched with the following parameters: [{run.id=2}]
2018-07-02 15:27:07.154  INFO 16337 --- [           main] o.s.batch.core.job.SimpleStepHandler     : Executing step: [step1]
2018-07-02 15:27:07.179  INFO 16337 --- [           main] c.e.demo.batch.PersonItemProcessor       : Converting (firstName: Jill, lastName: Doe) into (firstName: JILL, lastName: DOE)
2018-07-02 15:27:07.179  INFO 16337 --- [           main] c.e.demo.batch.PersonItemProcessor       : Converting (firstName: Joe, lastName: Doe) into (firstName: JOE, lastName: DOE)
2018-07-02 15:27:07.179  INFO 16337 --- [           main] c.e.demo.batch.PersonItemProcessor       : Converting (firstName: Justin, lastName: Doe) into (firstName: JUSTIN, lastName: DOE)
2018-07-02 15:27:07.180  INFO 16337 --- [           main] c.e.demo.batch.PersonItemProcessor       : Converting (firstName: Jane, lastName: Doe) into (firstName: JANE, lastName: DOE)
2018-07-02 15:27:07.180  INFO 16337 --- [           main] c.e.demo.batch.PersonItemProcessor       : Converting (firstName: John, lastName: Doe) into (firstName: JOHN, lastName: DOE)
2018-07-02 15:27:07.206  INFO 16337 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [SimpleJob: [name=importUserJob]] completed with the following parameters: [{run.id=1}] and the following status: [COMPLETED]
```
- 可以看到`people`表多了一些数据，并且`spring data jpa`为`spring batch`生成了一些表

> ![image.png](https://upload-images.jianshu.io/upload_images/7176877-502ad430c70e05e7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)