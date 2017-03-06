# Step 2. Workspace Data Model and First Resource

## Objectives
The next step is to start to define the data model and build out JAX-RS resources that 
implement this model. In practice, it is best to spend some time up front thinking about the
REST API and how the different parts will fit together to meet your business objectives and 
requirements. For this tutorial, we'll work through the API in a step-wise fashion to 
introduce key features.

## Core Concepts
The workspace is the container that will display panels and allow users to manipulate those 
panels. These will be implemented by external applications that determine the specifics of how 
information should be displayed and integrate with custom content providers (e.g., an application
that allows users to manage their twitter feed would provided panels to create searches, display 
their timeline, display individual tweets, etc. Workspaces will have basic descriptive information 
(workspace metadata) along with the panels that define the content. We will likely extend that
model over time, but this seems like a good starting point.

Workspaces will be created and owned by users and may be organized into groups and shared 
between users. We'll re-visit some of those details as we move forward. We want to support 
user-friendly URLs by defining scopes (to organize a collection of workspaces) and keys (unique 
identifiers for a workspace within a scope. For example, a user might have a personal scope, 
`@audenaert` with workspaces for `vacation_krakow`, `twitter_dh`, and `twitter_aggies` that he 
uses to plan a vacation to Krakow and to curate his Twitter feed for Digital Humanities 
and Texas A&M related content respectfully. We'll come back to using those scopes later.
 
For now, we'll access workspaces using an assigned unique identifier. Each workspace will have 
a name and a description to support basic display and discovery. We'll also use a version number
to keep track of changes to the workspace (including both its metadata and content). This version 
number will be a unique, opaque token that will be updated each time the workspace changes. 
See the (since deprecated) [Twitter Snowflake](https://github.com/twitter/snowflake/tree/snowflake-2010)
for a discussion of design decisions related to generating identifiers in a high-volume environment. 
Because something like this might be needed (should we opt to scale out vWise in the future), 
we'll leave some space in our design of unique identifiers to accommodate this rather than 
using integers or UUIDs directly (though we will likely use that under the hood for the initial 
implementation).

## API Definition
We'll define our REST API using the [OpenAPI specification (Swagger)](https://swagger.io). Our 
Swagger API documentation is given in [rest-api.yml](./rest-api.yml). You can copy this file 
into the [online editor](http://editor.swagger.io/#!/) to see the formatted output.

To start with, we'll define two resources, one for the collection of all workspaces 
and one for a single workspace. We will also define a data structure to represent the metadata
about a workspace called `WorkspaceMeta`. 

The workspace collection (`/workspaces`) will support listing all workspaces using `GET` and 
creating a new workspace using `POST`. We'll add support scope and key identification, 
authentication and other capabilities later. 

The individual workspace (`/workspaces/{wsId}`) will support retrieval using `GET`, updates
using `PUT` and removal using `DELETE`. Notably, the `PUT` method is a bit complex since we
want to apply only changes from the last version of the workspace and leave untouched any 
modifications to other fields that may have been made since we checked the state of the workspace.
Eventually, we may come back and add support for patch updates.

## Domain Model
Along the way, we are going to need some form of backing store for this data. One option would be
to allow the REST API to directly connect to a database. That approach isn't likely to be 
maintainable. A more robust approach would be to create a fully separate backing layer with its 
own isolated data model defined using Java Interfaces. This is more flexible, but requires a 
translation/adapting step from the data models used in the REST API that results in a lot of 
extra coding effort. Moreover, since we only envision this being implemented as a REST service,
the translation layer seems unnecessary.

As a hybrid approach, we'll design a shared data model that will be used in the REST API and 
supplied directly to and from the persistence layer.  More over, we'll follow the basic 
principles of [Domain Driven Design (DDD)](https://en.wikipedia.org/wiki/Domain-driven_design).
Basically that means that we'll use *Entity*, *Value Object* and *Aggregate* classes to represent 
data in our system. *Repository* classes will manage access to the persistence layer and  
*Service* classes will provide operations that do not logically belong within an object.

The `VwiseApplicationContext` will provides the primary entry point for accessing the various 
implementation specific components of the system. As our application moves toward production, 
this will be injected using some form of dependency injection. For now, we will simply 
instantiate the appropriate implementation as needed.

A `WorkspaceRepository` will provide basic CRUD operations for repositories. Instances of this
repository will be obtained from the application context. Eventually, the process of retrieving 
a repository may bind the repository to a specific context (e.g., supplying an authenticated 
user to support access control and auditing). This allows a relatively clean API that does not 
directly reference user accounts or other contextual information.

The entities, value objects and aggregates will be created within the `model` sub-package. At 
this point, all that is needed is the workspace metadata. 
  
## JAX-RS API Implementation 
To implement the REST API we'll use JAX-RS. As discussed, we'll use the data model defined in 
the domain model. The workspace collections resources will serve as the only top level JAX-RS
resource (at least for now), implemented in the `WorkspaceCollectionResource` class. The 
individual workspace resources will be returned as a sub-resource from the collection (as
instances of the `WorkspaceResource` class). 

## Testing
We will use unit tests both to ensure correct implementation of the internals as well as to 
ensure that the API has been implemented correctly and behaves as specified. Notably, the REST 
API based tests can serve as the starting point for a client implementation and as a tool for 
verifying any future implementations of the API (for example, an Express backed implementation)
that may be developed.