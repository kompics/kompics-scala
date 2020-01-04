.. _testing:

Testing
==========

Kompics provides a module ``kompics-testing`` for unit testing components. This includes a DSL for writing test cases by describing the events that go in and out of a single component under test.

.. code-block:: xml

  <dependency>
    <groupId>se.sics.kompics.testing</groupId>
    <artifactId>kompics-testing</artifactId> 
    <version>${kompics.version}</version>   
  </dependency>

.. toctree::
   :maxdepth: 2

   basics/basics
   answering_requests/answering_requests
