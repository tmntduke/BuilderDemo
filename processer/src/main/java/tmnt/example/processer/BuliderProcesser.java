package tmnt.example.processer;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by tmnt on 2017/11/16.
 */
@AutoService(Processor.class)
public class BuliderProcesser extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Bulider.class.getCanonicalName());
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(Bulider.class)) {

            if (element.getKind() != ElementKind.CLASS)
                return false;

            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            String elementName = element.getSimpleName().toString();
            ClassName builderClassName = ClassName.get(packageName, String.format("%sBuilder", elementName));
            TypeSpec typeSpec= createTypeSpec(element, builderClassName, elementName);
            JavaFile file=JavaFile.builder(packageName,typeSpec).build();
            try {
                file.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return true;
    }

    private Set<Element> getFields(Element element) {
        Set<Element> fields = new LinkedHashSet<>();
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                fields.add(enclosedElement);
            }
        }
        return fields;
    }

    private TypeSpec createTypeSpec(Element element, ClassName builderClassName, String elementName) {
        Set<Element> fieldElements = getFields(element);
        List<FieldSpec> fieldSpecs = new ArrayList<>(fieldElements.size());
        List<MethodSpec> setterSpecs = new ArrayList<>(fieldElements.size());
        for (Element field : fieldElements) {
            TypeName fieldType = TypeName.get(field.asType());
            String fieldName = field.getSimpleName().toString();
            FieldSpec fieldSpec = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE).build();
            fieldSpecs.add(fieldSpec);
            MethodSpec setterSpec = MethodSpec
                    .methodBuilder(fieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(builderClassName)
                    .addParameter(fieldType, fieldName)
                    .addStatement("this.$N = $N", fieldName, fieldName)
                    .addStatement("return this")
                    .build();
            setterSpecs.add(setterSpec);
        }

        //builder 方法
        TypeName elementType = TypeName.get(element.asType());
        String instanceName = Helper.toCamelCase(elementName);
        MethodSpec.Builder buildMethodBuilder = MethodSpec
                .methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(elementType)
                .addStatement("$1T $2N = new $1T()", elementType, instanceName);

        for (FieldSpec fieldSpec : fieldSpecs) {
            buildMethodBuilder.addStatement("$1N.$2N = $2N", instanceName, fieldSpec);
        }
        buildMethodBuilder.addStatement("return $N", instanceName);
        MethodSpec buildMethod = buildMethodBuilder.build();

        return TypeSpec
                .classBuilder(builderClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(fieldSpecs)
                .addMethods(setterSpecs)
                .addMethod(buildMethod)
                .build();


    }

}
