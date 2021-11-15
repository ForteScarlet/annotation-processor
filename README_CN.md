# Annotation-Tool

中文 | [English](README.md)

这是一个服务于注解的工具库，一个小巧可爱的库。

## 使用

**核心模块：** [核心](core)

## 速览

首先，你需要得到一个 `AnnotationTool` 实例：

```java
AnnotationTool tool=AnnotationTools.getAnnotationTool();
```

### 创建注解实例

```java
    public void test1()throws ReflectiveOperationException {
        Map<String, Object> params=new HashMap<>();
        params.put("value","Hello World");
        params.put("size",15);
        params.put("name","ForteScarlet");
        // throws ReflectiveOperationException
        final Element annotationInstance1=tool.createAnnotationInstance(Element.class,params);
        assert annotationInstance1.name().equals("ForteScarlet");
        assert annotationInstance1.size()==15;
        assert annotationInstance1.value().equals("Hello World");

        params.remove("name");
        final Element annotationInstance2=tool.createAnnotationInstance(Element.class,params);
        // default value support.
        assert annotationInstance2.name().equals("forte");
        assert annotationInstance2.size()==15;
        assert annotationInstance2.value().equals("Hello World");

        final Element annotationInstance3=tool.createAnnotationInstance(Element.class,params);
        assert annotationInstance2.equals(annotationInstance3);

        final Element nativeElement=ExampleMain.class.getAnnotation(Element.class);
        assert nativeElement.equals(annotationInstance3);
}
```