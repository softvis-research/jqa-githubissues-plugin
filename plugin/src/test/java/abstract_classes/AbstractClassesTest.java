package abstract_classes;

import jdom.XMLParser;
import org.junit.Test;
import toolbox.MarkdownParser;
import toolbox.StoreTool;

/**
 * Covers the following problem:
 *
 * Java creates a default constructor even for abstract classes.
 * To improve the code coverage for abstract classes this test was written.
 *
 * See: https://sourceforge.net/p/cobertura/bugs/17/
 */
public class AbstractClassesTest {

    @Test
    public void coverAbstractClasses() {

        new XMLParser() {};
        new MarkdownParser() {};
        new StoreTool() {};
    }
}
