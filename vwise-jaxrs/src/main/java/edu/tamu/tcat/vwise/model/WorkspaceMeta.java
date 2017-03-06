package edu.tamu.tcat.vwise.model;

/**
 * Provides basic descriptive information (workspace metadata) about a workspace.
 *
 * A workspace is a container that that displays panels and allow users to manipulate those
 * panels. The interfaces that allow users to interact with a workspace are implemented by
 * external applications that determine the specifics of how information should be displayed,
 * manage the application-supported custom panel extensions and integrate with content
 * providers (e.g., an application that allows users to manage their twitter feed might
 * provide panels to create searches, display their timeline, display individual tweets, etc).
 *
 * Workspaces are created and owned by users and may be organized into groups and shared
 * between users. To support user-friendly URLs, panels a workspace is associated with a
 * scope (to organize a collection of workspaces) and key (a unique, user-defined identifier
 * for a workspace within a scope). For example, a user might have a personal scope,
 * `@audenaert` with workspaces for `vacation_krakow`, `twitter_dh`, and `twitter_aggies` that he
 * uses to plan a vacation to Krakow and to curate Twitter feeds for Digital Humanities
 * and Texas A&M related content respectfully.
 */
public class WorkspaceMeta
{
   /**
    * Creates a duplicate of the supplied workspace metadata instance.
    *
    * @param orig the workspace to copy.
    * @return A copy of the supplied workspace metadata.
    */
   public static WorkspaceMeta copy(WorkspaceMeta orig)
   {
     WorkspaceMeta ws = new WorkspaceMeta();
     ws.id = orig.id;
     ws.version = orig.version;
     ws.scope = orig.scope;
     ws.key = orig.key;
     ws.name = orig.name;
     ws.description = orig.description;

     return ws;
   }

   /** A unique, persistent identifier for this workspace. */
   public String id;

   /**
    * Version identifier that tracks different states of the workspace (including both
    * its metadata and content). This version number is a unique, opaque token that
    * that will be updated each time the workspace changes. See the (since deprecated)
    * [Twitter Snowflake](https://github.com/twitter/snowflake/tree/snowflake-2010)
    * for a discussion of design decisions related to generating identifiers in a
    * high-volume environment. While this level of complexity is not currently needed
    * or implemented, an opaque, string-based identifier is used in lieu of a numeric,
    * UUID or other explicit scheme in order to allow space to use a more complex version
    * identification scheme in the future as needed.
    */
   public String version;

   /**
    * Defines a grouping of workspaces in order to facilitate personal and group-based
    * organization of workspaces and to provide user-friendly workspace URIs. Scope names
    * are constrained by their types. Personal scopes begin with `@` (e.g. `@audenaert`)
    * and group-based scopes begin with a `~` (e.g., `~tamu.cpsc410.spring2017` for a
    * group related to the Spring 2017 CPSC 410 class at Texas A&M University).
    *
    * Workspaces within a scope are identified by a unique key.
    */
   public String scope;

   /**
    * User-defined identifier for a workspace. This key must be unique within the
    * associated scope. This is intended to be an easily readable identifier that
    * is either user-generated or a sluggified form of the workspace name.
    */
   public String key;

   /** The name of this workspace for display and discovery. */
   public String name;

   /** A brief description of this workspace. */
   public String description;

}