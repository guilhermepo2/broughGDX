package com.broughgdx.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.broughgdx.broughGDX;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                // Resizable application, uses available space in browser
                // return new GwtApplicationConfiguration(true);
                // Fixed size application:
                return new GwtApplicationConfiguration(640, 420);
        }

        @Override
        public ApplicationListener createApplicationListener () {
                return new broughGDX();
        }
}