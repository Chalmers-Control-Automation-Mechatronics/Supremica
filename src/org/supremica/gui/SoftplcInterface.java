package org.supremica.gui;

import java.io.File;

public class SoftplcInterface extends File {

        private SoftplcInterface() {
                super("");
        }

        public SoftplcInterface(String path) {
                super(path);
        }

        public String toString() {
                return super.getName();
        }

}