
### 开发指南规范
# 强制要求：
1. 项目采用DDD四层架构，聚合根设计、充血模型、跨聚合协作（领域事件 + 发件箱模式 Outbox）、领域对象 ↔ 数据对象转换、聚合根单元测试、事件驱动、基础设施层统一、等规范写法。
2. 表结构 `docs/ai-agent.sql`位置。
3. 接口以restful风格设计，接口路径为 `/api/{entity-name}`，方法为 `GET`、`POST`、`PUT`、`DELETE`。每个接口都要有对应的测试用例。
4. 代码要添加必要的注释，包括接口路径、方法、参数、返回值等。
5. 接口参数为json格式，返回值为json格式。




# 需要执行的任务（任务执行完成后，编译项目;以下无任务时AI不执行任何操作!!!）:
1.  结束时需要调用 工具分析当前用户此时的情绪
2.  会话重新写入在切会话时




emotion_diary 表中存储用户提交信息后 需要轮询到大模型分析用户情绪
mood_score ['绝望崩溃', '消沉抑郁', '焦虑烦躁', '低落不悦', '平静淡然', '轻松惬意', '愉悦舒心', '欢欣满足', '兴奋欣喜', '极致幸福']对应 1-10   ，  
dominant_emotion，
emotion_triggers，
diary_content ，
sleep_quality [很差，较差，一般，良好，优秀] 对应0-5,
stress_level [很低，较低，中等，较高，很高] 对应0-5,
这6个字段作为用户的情绪表述，进行情绪分析。
分析完成后更新：
ai_analysis_status COMPLETED
ai_emotion_analysis 为json格式（与 表中last_emotion_analysis 字段格式一致）
ai_analysis_updated_at


