/*
 *  Copyright (C) 2007 - 2014 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.geoserver.geofence;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;

import static junit.framework.TestCase.assertTrue;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.geofence.config.GeoFencePropertyPlaceholderConfigurer;
import org.geoserver.geofence.utils.GeofenceTestUtils;
import org.geoserver.geofence.web.GeofencePage;
import org.geoserver.web.GeoServerHomePage;
import org.geoserver.web.GeoServerWicketTestSupport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.UrlResource;

public class GeofencePageTest extends GeoServerWicketTestSupport {

    static GeoFencePropertyPlaceholderConfigurer configurer;

    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);

        // get the beans we use for testing
        configurer = (GeoFencePropertyPlaceholderConfigurer) applicationContext
                .getBean("geofence-configurer");
        configurer.setLocation(
                new UrlResource(this.getClass().getResource("/test-config.properties")));
    }

    @Before
    public void before() {
        login();
        tester.startPage(GeofencePage.class);
    }

    /**
     * @FIXME This test fails in 2.6
     */
    @Ignore
    @Test
    public void testSave() throws URISyntaxException, IOException {
        GeofenceTestUtils.emptyFile("test-config.properties");
        FormTester ft = tester.newFormTester("form");
        ft.submit("submit");
        tester.assertRenderedPage(GeoServerHomePage.class);

        File configFile = configurer.getConfigFile().file();
        LOGGER.info("Config file is " + configFile);

        assertTrue(GeofenceTestUtils.readConfig(configFile).length() > 0);
    }

    /**
     * @FIXME This test fails in 2.6
     */
    @Ignore
    @Test
    public void testCancel() throws URISyntaxException, IOException {
        GeofenceTestUtils.emptyFile("test-config.properties");
        // GeofenceTestUtils.emptyFile("test-cache-config.properties");
        FormTester ft = tester.newFormTester("form");
        ft.submit("cancel");
        tester.assertRenderedPage(GeoServerHomePage.class);
        assertTrue(GeofenceTestUtils.readConfig("test-config.properties").length() == 0);
        // assertTrue(GeofenceTestUtils.readConfig("test-cache-config.properties").length() == 0);
    }

    @Test
    public void testErrorEmptyInstance() {
        FormTester ft = tester.newFormTester("form");
        ft.setValue("instanceName", "");
        ft.submit("submit");
        tester.assertRenderedPage(GeofencePage.class);

        tester.assertContains("is required");
    }

    @Test
    public void testErrorEmptyURL() {
        FormTester ft = tester.newFormTester("form");
        ft.setValue("servicesUrl", "");
        ft.submit("submit");
        tester.assertRenderedPage(GeofencePage.class);

        tester.assertContains("is required");
    }

    @Test
    public void testErrorWrongURL() {
        @SuppressWarnings("unchecked")
        TextField<String> servicesUrl = ((TextField<String>) tester
                .getComponentFromLastRenderedPage("form:servicesUrl"));
        servicesUrl.setDefaultModel(new Model<String>("fakeurl"));

        tester.clickLink("form:test", true);

        tester.assertContains("RemoteAccessException");
    }

    @Test
    public void testErrorEmptyCacheSize() {
        FormTester ft = tester.newFormTester("form");
        ft.setValue("cacheSize", "");
        ft.submit("submit");
        tester.assertRenderedPage(GeofencePage.class);

        tester.assertContains("is required");
    }

    @Test
    public void testErrorWrongCacheSize() {
        FormTester ft = tester.newFormTester("form");
        ft.setValue("cacheSize", "A");
        ft.submit("submit");
        tester.assertRenderedPage(GeofencePage.class);

        tester.assertContains("long");
    }

    @Test
    public void testInvalidateCache() {
        tester.clickLink("form:invalidate", true);
        String success = new StringResourceModel(
                GeofencePage.class.getSimpleName() + ".cacheInvalidated").getObject();
        tester.assertInfoMessages((Serializable[]) new String[] { success });
    }

}
