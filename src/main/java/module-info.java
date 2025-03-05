module com.mojang.datafixerupper {

    requires com.google.common;
    requires it.unimi.dsi.fastutil;
    requires org.jspecify;
    requires org.slf4j;
    requires com.google.gson;

    exports com.mojang.datafixers;
    exports com.mojang.serialization;
}