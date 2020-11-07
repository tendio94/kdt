package com.tendio.kdt.executor.actions.doc;

import com.tendio.kdt.executor.actions.annotation.ActionDescription;
import com.tendio.kdt.executor.actions.model.ActionMethod;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;

public final class DocGenerator {
    private static final String HTML_DOCUMENT_HEAD = "<html title=\"Actions Descriptor\"><head><style>" +
            "th{font-size: large} td{background-color: lightgreen;color:darkred}</style></head><body>";
    private static final String HTML_ROW_STRING = "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";
    private static final String HTML_TABLE_STRING = "<table><th>Class</th><th>Method</th><th>Definition</th><th>Description</th>";
    private static final String HTML_DOCUMENT_CLOSURE = "</table></body></html>";
    private static final String DOC_PATH = "report/actions.html";
    private static StringBuilder sb;

    private DocGenerator() {
    }

    //find all classes @ActionClass
    //find all methods @ActionDefinition
    //generateForActionMethods html table row for each method with columns: Package, Method(with signature), Definition(@ActionDefinition), Description(@ActionDescription)

    //TODO
    public static void generateForActionMethods(List<ActionMethod> actionMethods) throws IOException {
        sb = new StringBuilder();
        sb.append(HTML_DOCUMENT_HEAD).append(HTML_TABLE_STRING);
        actionMethods.forEach(DocGenerator::appendRow);
        sb.append(HTML_DOCUMENT_CLOSURE);
        FileUtils.write(new File(DOC_PATH), sb.toString(), Charset.defaultCharset());

    }

    private static void appendRow(ActionMethod actionMethod) {
        Method method = actionMethod.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String definition = actionMethod.getMappingDefinition();
        ActionDescription annotation = method.getAnnotation(ActionDescription.class);
        String description = annotation != null ? annotation.value() : "";

        String rowContent = String.format(HTML_ROW_STRING, className, methodName, definition, description);
        sb.append(rowContent);
    }

}
