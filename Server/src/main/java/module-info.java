module RidesharingServer {
    requires org.locationtech.jts;

    exports server.service;
    exports server.tools;
    exports server.model.users;
    exports server.model.tree;
    exports server.model;
}