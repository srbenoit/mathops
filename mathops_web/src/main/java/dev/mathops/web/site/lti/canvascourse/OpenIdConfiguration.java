package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.text.parser.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for data extracted from the JSON OpenID configuration retrieved from the LMS.
 *
 * <pre>
 * {
 *   "issuer":"https://domino.math.colostate.edu",
 *   "authorization_endpoint":"https://domino.math.colostate.edu:20443/api/lti/authorize_redirect",
 *   "registration_endpoint":"https://domino.math.colostate.edu:20443/api/lti/registrations",
 *   "jwks_uri":"https://domino.math.colostate.edu:20443/api/lti/security/jwks",
 *   "token_endpoint":"https://domino.math.colostate.edu:20443/login/oauth2/token",
 *   "token_endpoint_auth_methods_supported":["private_key_jwt"],
 *   "token_endpoint_auth_signing_alg_values_supported":["RS256"],
 *   "scopes_supported":[
 *     "openid",
 *     "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem",
 *     "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly",
 *     "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly",
 *     "https://purl.imsglobal.org/spec/lti-ags/scope/score",
 *     "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly",
 *     "https://canvas.instructure.com/lti/public_jwk/scope/update",
 *     "https://canvas.instructure.com/lti/account_lookup/scope/show",
 *     "https://canvas.instructure.com/lti-ags/progress/scope/show",
 *     "https://canvas.instructure.com/lti/page_content/show"
 *   ],
 *   "response_types_supported":["id_token"],
 *   "id_token_signing_alg_values_supported":["RS256"],
 *   "claims_supported":["sub","picture","email","name","given_name","family_name","locale"],
 *   "subject_types_supported":["public"],
 *   "authorization_server":"domino.math.colostate.edu",
 *   "https://purl.imsglobal.org/spec/lti-platform-configuration":{
 *     "product_family_code":"canvas",
 *     "version":"OpenSource",
 *     "messages_supported":[
 *       {
 *         "type":"LtiResourceLinkRequest",
 *         "placements":[
 *           "https://canvas.instructure.com/lti/account_navigation",
 *           "https://canvas.instructure.com/lti/analytics_hub",
 *           "https://canvas.instructure.com/lti/assignment_edit",
 *           "https://canvas.instructure.com/lti/assignment_group_menu",
 *           "https://canvas.instructure.com/lti/assignment_index_menu",
 *           "https://canvas.instructure.com/lti/assignment_menu",
 *           "https://canvas.instructure.com/lti/assignment_selection",
 *           "https://canvas.instructure.com/lti/assignment_view",
 *           "https://canvas.instructure.com/lti/collaboration"
 *           "https://canvas.instructure.com/lti/conference_selection",
 *           "https://canvas.instructure.com/lti/course_assignments_menu",
 *           "https://canvas.instructure.com/lti/course_home_sub_navigation",
 *           "https://canvas.instructure.com/lti/course_navigation",
 *           "https://canvas.instructure.com/lti/course_settings_sub_navigation",
 *           "https://canvas.instructure.com/lti/discussion_topic_index_menu",
 *           "https://canvas.instructure.com/lti/discussion_topic_menu",
 *           "https://canvas.instructure.com/lti/file_index_menu",
 *           "https://canvas.instructure.com/lti/file_menu",
 *           "https://canvas.instructure.com/lti/global_navigation",
 *           "https://canvas.instructure.com/lti/homework_submission",
 *           "https://canvas.instructure.com/lti/link_selection",
 *           "https://canvas.instructure.com/lti/migration_selection",
 *           "https://canvas.instructure.com/lti/module_group_menu",
 *           "https://canvas.instructure.com/lti/module_index_menu",
 *           "https://canvas.instructure.com/lti/module_index_menu_modal",
 *           "https://canvas.instructure.com/lti/module_menu_modal",
 *           "https://canvas.instructure.com/lti/module_menu",
 *           "https://canvas.instructure.com/lti/post_grades",
 *           "https://canvas.instructure.com/lti/quiz_index_menu",
 *           "https://canvas.instructure.com/lti/quiz_menu",
 *           "https://canvas.instructure.com/lti/similarity_detection",
 *           "https://canvas.instructure.com/lti/student_context_card",
 *           "https://canvas.instructure.com/lti/submission_type_selection",
 *           "https://canvas.instructure.com/lti/tool_configuration",
 *           "https://canvas.instructure.com/lti/top_navigation",
 *           "https://canvas.instructure.com/lti/user_navigation",
 *           "https://canvas.instructure.com/lti/wiki_index_menu",
 *           "https://canvas.instructure.com/lti/wiki_page_menu",
 *           "ContentArea"
 *         ]
 *       },
 *       {
 *        "type":"LtiDeepLinkingRequest",
 *        "placements":[
 *          "https://canvas.instructure.com/lti/assignment_selection",
 *          "https://canvas.instructure.com/lti/collaboration",
 *          "https://canvas.instructure.com/lti/conference_selection",
 *          "https://canvas.instructure.com/lti/course_assignments_menu",
 *          "https://canvas.instructure.com/lti/editor_button",
 *          "https://canvas.instructure.com/lti/homework_submission",
 *          "https://canvas.instructure.com/lti/link_selection",
 *          "https://canvas.instructure.com/lti/migration_selection",
 *          "https://canvas.instructure.com/lti/module_index_menu_modal",
 *          "https://canvas.instructure.com/lti/module_menu_modal",
 *          "https://canvas.instructure.com/lti/submission_type_selection",
 *          "ContentArea","RichTextEditor"
 *        ]
 *       }
 *     ],
 *     "variables":[
 *       "ResourceLink.id",
 *       "ResourceLink.description",
 *       "ResourceLink.title",
 *       "ResourceLink.available.startDateTime",
 *       "ResourceLink.available.endDateTime",
 *       "ResourceLink.submission.endDateTime",
 *       "com.instructure.User.observees",
 *       "com.instructure.User.sectionNames",
 *       "com.instructure.RCS.app_host",
 *       "com.instructure.User.student_view",
 *       "com.instructure.RCS.service_jwt",
 *       "com.instructure.instui_nav",
 *       "com.instructure.Observee.sisIds",
 *       "Context.title",
 *       "com.instructure.Editor.contents",
 *       "com.instructure.Editor.selection",
 *       "com.instructure.PostMessageToken",
 *       "com.instructure.Assignment.lti.id",
 *       "com.instructure.Assignment.description",
 *       "com.instructure.Assignment.allowedFileExtensions",
 *       "com.instructure.OriginalityReport.id",
 *       "com.instructure.Submission.id",
 *       "com.instructure.File.id",
 *       "CourseOffering.sourcedId",
 *       "Context.id",
 *       "com.instructure.Context.globalId",
 *       "com.instructure.Context.uuid",
 *       "Context.sourcedId",
 *       "Context.id.history",
 *       "Message.documentTarget",
 *       "Message.locale",
 *       "ToolConsumerInstance.guid",
 *       "Canvas.api.domain",
 *       "Canvas.api.collaborationMembers.url",
 *       "Canvas.api.baseUrl",
 *       "ToolProxyBinding.memberships.url",
 *       "Canvas.account.id",
 *       "Canvas.account.name",
 *       "Canvas.account.sisSourceId",
 *       "Canvas.rootAccount.id",
 *       "Canvas.rootAccount.sisSourceId",
 *       "Canvas.externalTool.global_id",
 *       "Canvas.externalTool.url",
 *       "com.instructure.brandConfigJSON.url",
 *       "com.instructure.brandConfigJSON",
 *       "com.instructure.brandConfigJS.url",
 *       "Canvas.css.common",
 *       "Canvas.shard.id",
 *       "Canvas.root_account.global_id",
 *       "Canvas.root_account.id",
 *       "vnd.Canvas.root_account.uuid",
 *       "Canvas.root_account.sisSourceId",
 *       "Canvas.course.id",
 *       "vnd.instructure.Course.uuid",
 *       "Canvas.course.name",
 *       "Canvas.course.sisSourceId",
 *       "com.instructure.Course.integrationId",
 *       "Canvas.course.startAt",
 *       "Canvas.course.endAt",
 *       "Canvas.course.workflowState",
 *       "Canvas.course.hideDistributionGraphs",
 *       "Canvas.course.gradePassbackSetting",
 *       "Canvas.term.startAt",
 *       "Canvas.term.endAt",
 *       "Canvas.term.name",
 *       "Canvas.term.id",
 *       "CourseSection.sourcedId",
 *       "Canvas.enrollment.enrollmentState",
 *       "com.instructure.Assignment.anonymous_grading",
 *       "com.instructure.Assignment.restrict_quantitative_data",
 *       "com.instructure.Course.gradingScheme",
 *       "com.Instructure.membership.roles",
 *       "Canvas.membership.roles",
 *       "Canvas.membership.concludedRoles",
 *       "Canvas.membership.permissions\u003c\u003e",
 *       "Canvas.course.previousContextIds",
 *       "Canvas.course.previousContextIds.recursive",
 *       "Canvas.course.previousCourseIds",
 *       "Person.name.full",
 *       "Person.name.display",
 *       "Person.name.family",
 *       "Person.name.given",
 *       "com.instructure.Person.name_sortable",
 *       "Person.email.primary",
 *       "com.instructure.Person.pronouns",
 *       "vnd.Canvas.Person.email.sis",
 *       "Person.address.timezone",
 *       "User.image",
 *       "User.id",
 *       "Canvas.user.id",
 *       "vnd.instructure.User.uuid",
 *       "vnd.instructure.User.current_uuid",
 *       "Canvas.user.prefersHighContrast",
 *       "com.instructure.Course.groupIds",
 *       "Canvas.group.contextIds",
 *       "Membership.role",
 *       "Canvas.xuser.allRoles",
 *       "com.instructure.User.allRoles",
 *       "Canvas.user.globalId",
 *       "Canvas.user.isRootAccountAdmin",
 *       "Canvas.user.adminableAccounts",
 *       "User.username",
 *       "Canvas.user.loginId",
 *       "Canvas.user.sisSourceId",
 *       "Canvas.user.sisIntegrationId",
 *       "Person.sourcedId",
 *       "Canvas.logoutService.url",
 *       "Canvas.masqueradingUser.id",
 *       "Canvas.masqueradingUser.userId",
 *       "Canvas.xapi.url",
 *       "Caliper.url",
 *       "Canvas.course.sectionIds",
 *       "Canvas.course.sectionRestricted",
 *       "Canvas.course.sectionSisSourceIds",
 *       "com.instructure.contextLabel",
 *       "Canvas.module.id","Canvas.moduleItem.id",
 *       "Canvas.assignment.id",
 *       "Canvas.assignment.description",
 *       "com.instructure.Group.id",
 *       "com.instructure.Group.name",
 *       "Canvas.assignment.title",
 *       "Canvas.assignment.pointsPossible",
 *       "Canvas.assignment.hideInGradebook",
 *       "Canvas.assignment.omitFromFinalGrade",
 *       "Canvas.assignment.unlockAt",
 *       "Canvas.assignment.lockAt",
 *       "Canvas.assignment.dueAt",
 *       "Canvas.assignment.unlockAt.iso8601",
 *       "Canvas.assignment.lockAt.iso8601",
 *       "Canvas.assignment.dueAt.iso8601",
 *       "Canvas.assignment.earliestEnrollmentDueAt.iso8601",
 *       "Canvas.assignment.allDueAts.iso8601",
 *       "Canvas.assignment.published",
 *       "Canvas.assignment.lockdownEnabled",
 *       "Canvas.assignment.allowedAttempts",
 *       "Canvas.assignment.submission.studentAttempts",
 *       "LtiLink.custom.url",
 *       "ToolProxyBinding.custom.url",
 *       "ToolProxy.custom.url",
 *       "ToolConsumerProfile.url",
 *       "vnd.Canvas.OriginalityReport.url",
 *       "vnd.Canvas.submission.url",
 *       "vnd.Canvas.submission.history.url",
 *       "Canvas.file.media.id","Canvas.file.media.type",
 *       "Canvas.file.media.duration",
 *       "Canvas.file.media.size",
 *       "Canvas.file.media.title",
 *       "Canvas.file.usageRights.name",
 *       "Canvas.file.usageRights.url",
 *       "Canvas.file.usageRights.copyrightText",
 *       "com.instructure.Course.accept_canvas_resource_types",
 *       "com.instructure.Course.canvas_resource_type",
 *       "com.instructure.Course.canvas_resource_id",
 *       "com.instructure.Course.allow_canvas_resource_selection",
 *       "com.instructure.Course.available_canvas_resources",
 *       "com.instructure.Account.usage_metrics_enabled",
 *       "com.instructure.user.lti_1_1_id.history",
 *       "LineItem.resultValue.max"
 *     ],
 *     "https://canvas.instructure.com/lti/account_name":"Domino Canvas",
 *     "https://canvas.instructure.com/lti/account_lti_guid":"HSJxKGMPNrGAasWEWi4J4yOEZBuHivuVpKGTQPPI:canvas-lms"
 *   }
 * }
 * </pre>
 */
final class OpenIdConfiguration {

    /** The issuer. */
    private final String issuer;

    /** The authorization endpoint. */
    private final String authorizationEndpoint;

    /** The registration endpoint. */
    private final String registrationEndpoint;

    /** The list of supported scopes. */
    private final List<String> scopesSupported;

    /** The list of supported claims. */
    private final List<String> claimsSupported;

    /** The list of resource link placements. */
    private final List<String> resourceLinkPlacements;

    /** The list of deep linking placements. */
    private final List<String> deepLinkingPlacements;

    /**
     * Constructs a new {@code OpenIdConfiguration} from a parsed JSON object.
     *
     * @param json the JSON object
     */
    OpenIdConfiguration(final JSONObject json) {

        this.issuer = json.getStringProperty("issuer");
        this.authorizationEndpoint = json.getStringProperty("authorization_endpoint");
        this.registrationEndpoint = json.getStringProperty("registration_endpoint");

        this.scopesSupported = new ArrayList<>(15);
        this.claimsSupported = new ArrayList<>(10);
        this.resourceLinkPlacements = new ArrayList<>(50);
        this.deepLinkingPlacements = new ArrayList<>(30);

        if (json.getProperty("scopes_supported") instanceof final Object[] scopesArray) {
            for (final Object o : scopesArray) {
                if (o instanceof final String s) {
                    this.scopesSupported.add(s);
                }
            }
        }

        if (json.getProperty("claims_supported") instanceof final Object[] claimsArray) {
            for (final Object o : claimsArray) {
                if (o instanceof final String s) {
                    this.claimsSupported.add(s);
                }
            }
        }

        if (json.getProperty("https://purl.imsglobal.org/spec/lti-platform-configuration")
                instanceof final JSONObject ims) {
            if (ims.getProperty("messages_supported") instanceof Object[] messagesArray) {
                for (final Object obj : messagesArray) {
                    if (obj instanceof JSONObject jsonEntry) {
                        final String type = jsonEntry.getStringProperty("type");
                        if ("LtiResourceLinkRequest".equals(type)) {
                            extractLtiResourceLinkRequest(jsonEntry);
                        } else if ("LtiDeepLinkingRequest".equals(type)) {
                            extractLtiDeepLinkingRequest(jsonEntry);
                        }
                    }
                }
            }
        }
    }

    /**
     * Extracts the array of Resource Link placements supported.
     *
     * @param jsonEntry the JSON object in which to find the "placements" array
     */
    private void extractLtiResourceLinkRequest(final JSONObject jsonEntry) {

        if (jsonEntry.getProperty("placements") instanceof Object[] placementsArray) {
            for (final Object o : placementsArray) {
                if (o instanceof final String s) {
                    this.resourceLinkPlacements.add(s);
                }
            }
        }
    }

    /**
     * Extracts the array of Deep Linking placements supported.
     *
     * @param jsonEntry the JSON object in which to find the "placements" array
     */
    private void extractLtiDeepLinkingRequest(final JSONObject jsonEntry) {

        if (jsonEntry.getProperty("placements") instanceof Object[] placementsArray) {
            for (final Object o : placementsArray) {
                if (o instanceof final String s) {
                    this.deepLinkingPlacements.add(s);
                }
            }
        }
    }

    /**
     * Gets the issuer.
     *
     * @return the issuer (null if none found)
     */
    String getIssuer() {

        return this.issuer;
    }

    /**
     * Gets the authorization endpoint.
     *
     * @return the authorization endpoint (null if none found)
     */
    String getAuthorizationEndpoint() {

        return this.authorizationEndpoint;
    }

    /**
     * Gets the registration endpoint.
     *
     * @return the registration endpoint (null if none found)
     */
    String getRegistrationEndpoint() {

        return this.registrationEndpoint;
    }

    /** The list of supported scopes. */
    List<String> getScopesSupported() {

        return this.scopesSupported;
    }

    /** The list of supported claims. */
    List<String> getClaimsSupported() {

        return this.claimsSupported;
    }

    /** The list of resource link placements. */
    List<String> getResourceLinkPlacements() {

        return this.resourceLinkPlacements;
    }

    /** The list of deep linking placements. */
    List<String> getDeepLinkingPlacements() {

        return this.deepLinkingPlacements;
    }
}
