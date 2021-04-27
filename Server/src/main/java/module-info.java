module RidesharingServer {
    requires org.locationtech.jts;
    requires java.sql;

    exports server.service;
    exports server.tools;
    exports server.model.users;
    exports server.model.tree;
    exports server;
}