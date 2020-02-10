package org.jqassistant.contrib.plugin.githubissues.abstract_classes;

import org.jqassistant.contrib.plugin.githubissues.jdom.XMLParser;
import org.junit.jupiter.api.Test;

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
    }
}
