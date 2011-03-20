package org.mockitousage.annotation;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.MockUtil;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockitousage.examples.use.ArticleCalculator;
import org.mockitousage.examples.use.ArticleDatabase;
import org.mockitousage.examples.use.ArticleManager;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MockInjectionUsingConstructorTest {
    private MockUtil mockUtil = new MockUtil();

    @Mock private ArticleCalculator calculator;
    @Mock private ArticleDatabase database;

    @InjectMocks private ArticleManager articleManager;
    @Spy @InjectMocks private ArticleManager spiedArticleManager;


    @InjectMocks private ArticleVisitor should_be_initialized_several_times;

    @Test
    public void shouldNotFailWhenNotInitialized() {
        assertNotNull(articleManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void innerMockShouldRaiseAnExceptionThatChangesOuterMockBehavior() {
        when(calculator.countArticles("new")).thenThrow(new IllegalArgumentException());

        articleManager.updateArticleCounters("new");
    }

    @Test
    public void mockJustWorks() {
        articleManager.updateArticleCounters("new");
    }

    @Test
    public void constructor_is_called_for_each_test() throws Exception {
        int number_of_test_before_including_this_one = 4;
        assertEquals(number_of_test_before_including_this_one, articleVisitorInstantiationCount);
        assertEquals(number_of_test_before_including_this_one, articleVisitorMockInjectedInstances.size());
    }

    @Test
    @Ignore("Works must be done on the spy annotation engine and the injection engine")
    public void objects_created_with_constructor_initialization_can_be_spied() throws Exception {
        assertFalse(mockUtil.isMock(articleManager));
        assertTrue(mockUtil.isMock(spiedArticleManager));
    }

    @Test
    @Ignore("Work should be done on error reporting on this matter")
    public void should_report_failure_only_when_object_initialization_throws_exception() throws Exception {

    }

    private static int articleVisitorInstantiationCount = 0;
    private static Set<Object> articleVisitorMockInjectedInstances = new HashSet<Object>();

    private static class ArticleVisitor {
        public ArticleVisitor(ArticleCalculator calculator) {
            articleVisitorInstantiationCount++;
            articleVisitorMockInjectedInstances.add(calculator);
        }
    }

}
