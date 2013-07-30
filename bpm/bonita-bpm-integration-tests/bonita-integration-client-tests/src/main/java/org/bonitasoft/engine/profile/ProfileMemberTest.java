package org.bonitasoft.engine.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class ProfileMemberTest extends AbstractProfileTest {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User" }, story = "Get profile for user.")
    @Test
    public void getProfileForUser() throws BonitaException, IOException {
        // Get Profile For User
        final List<Profile> profiles = getProfileAPI().getProfilesForUser(user1.getId());
        assertEquals(1, profiles.size());
        assertEquals("Administrator", profiles.get(0).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Group" }, story = "Get profile for user must return same profil only once", jira = "ENGINE-979")
    @Test
    public void getProfileForUserReturnDisctinctProfiles() throws BonitaException, IOException {
        // user 1 is mapped to profile "Administrator" through direct "userName1" mapping + through "role1" mapping:
        getIdentityAPI().addUserMembership(user1.getId(), group2.getId(), role1.getId());

        // Get Profile For User
        final List<Profile> getUserProfiles = getProfileAPI().getProfilesForUser(user1.getId());

        assertEquals(1, getUserProfiles.size());
        assertEquals("Administrator", getUserProfiles.get(0).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Group" }, story = "Get profile for user from group.")
    @Test
    public void getProfileForUserFromGroup() throws BonitaException, IOException {
        getIdentityAPI().addUserMembership(user1.getId(), group1.getId(), role1.getId());

        // Get Profile For User
        final List<Profile> getUserProfiles = getProfileAPI().getProfilesForUser(user1.getId());
        assertEquals(2, getUserProfiles.size());
        assertEquals("Administrator", getUserProfiles.get(0).getName());
        assertEquals("User", getUserProfiles.get(1).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Role" }, story = "Get profile for user from role.")
    @Test
    public void getProfileForUserFromRole() throws BonitaException, IOException {
        getIdentityAPI().addUserMembership(user1.getId(), group1.getId(), role1.getId());

        // Get Profile For User
        final List<Profile> getUserProfiles = getProfileAPI().getProfilesForUser(user1.getId());
        assertEquals(2, getUserProfiles.size());
        assertEquals("Administrator", getUserProfiles.get(0).getName());
        assertEquals("User", getUserProfiles.get(1).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Group", "Role" }, story = "Get profile for user from role and group.")
    @Test
    public void getProfileForUserFromRoleAndGroup() throws BonitaException, IOException {
        getIdentityAPI().addUserMembership(user5.getId(), group3.getId(), role3.getId());
        getIdentityAPI().addUserMembership(user2.getId(), group2.getId(), role3.getId());
        getIdentityAPI().addUserMembership(user3.getId(), group3.getId(), role2.getId());

        // Get Profile For User
        List<Profile> userProfiles = getProfileAPI().getProfilesForUser(user5.getId());
        assertEquals(1, userProfiles.size());
        assertEquals("Process owner", userProfiles.get(0).getName());

        // For profile "Process owner", good group, but bad role:
        userProfiles = getProfileAPI().getProfilesForUser(user2.getId());
        assertEquals(0, userProfiles.size());

        // For profile "Process owner", good role, but bad group:
        userProfiles = getProfileAPI().getProfilesForUser(user3.getId());
        assertEquals(1, userProfiles.size());
        assertEquals("Administrator", userProfiles.get(0).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Wrong parameter" }, story = "Execute profile member command with wrong parameter", jira = "ENGINE-586")
    @Test
    public void getProfilesForUserWithWrongParameter() throws Exception {
        final List<Profile> profilesForUser = getProfileAPI().getProfilesForUser(564162L);
        assertEquals(0, profilesForUser.size());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Search" }, story = "Search user profile members for proile.")
    @Test
    public void searchUserProfileMembersForProfile() throws BonitaException, IOException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, "User1FirstName");
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        builder.searchTerm("userName1");
        final SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("user", builder.done());
        assertEquals(1, searchedProfileMember.getResult().size());
        assertEquals("User1FirstName", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
        assertEquals("User1LastName", searchedProfileMember.getResult().get(0).getDisplayNamePart2());
        assertEquals("userName1", searchedProfileMember.getResult().get(0).getDisplayNamePart3());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Group", "Search" }, story = "Search group profile members for profile.")
    @Test
    public void searchGroupProfileMembersForProfile() throws BonitaException, IOException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, userProfileId);
        final SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("group", builder.done());

        assertEquals(1, searchedProfileMember.getResult().size());
        final ProfileMember profileMember = searchedProfileMember.getResult().get(0);
        assertEquals("group1", profileMember.getDisplayNamePart1());
        final String displayNamePart3 = profileMember.getDisplayNamePart3();
        // Can be null with Oracle
        assertTrue(displayNamePart3 == null || displayNamePart3.isEmpty());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Role", "Search" }, story = "Search role profile members for profile.")
    @Test
    public void searchRoleProfileMembersForProfile() throws BonitaException, IOException {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, "role2");
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        builder.searchTerm("role2");
        SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("role", builder.done());
        assertEquals(1, searchedProfileMember.getCount());
        ProfileMember profileMember = searchedProfileMember.getResult().get(0);
        assertEquals("role2", profileMember.getDisplayNamePart1());

        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        searchedProfileMember = getProfileAPI().searchProfileMembers("role", builder.done());
        profileMember = searchedProfileMember.getResult().get(0);
        assertEquals(2, searchedProfileMember.getCount());
        assertEquals("role1", profileMember.getDisplayNamePart1());
        final String displayNamePart2 = profileMember.getDisplayNamePart2();
        // Can be null with Oracle
        assertTrue(displayNamePart2 == null || displayNamePart2.isEmpty());
        final String displayNamePart3 = profileMember.getDisplayNamePart3();
        assertTrue(displayNamePart3 == null || displayNamePart3.isEmpty());
        assertEquals("role2", searchedProfileMember.getResult().get(1).getDisplayNamePart1());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Role", "Group", "Search" }, story = "Search role and group of profile members for profile.")
    @Test
    public void searchRoleAndGroupProfileMembersForProfile() throws BonitaException, IOException {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART2, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("roleAndGroup", builder.done());
        assertEquals(2, searchedProfileMember.getCount());
        assertEquals("role2", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
        assertEquals("group1", searchedProfileMember.getResult().get(0).getDisplayNamePart2());
        assertEquals("role2", searchedProfileMember.getResult().get(1).getDisplayNamePart1());
        assertEquals("group2", searchedProfileMember.getResult().get(1).getDisplayNamePart2());

        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART2, "group2");
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        searchedProfileMember = getProfileAPI().searchProfileMembers("roleAndGroup", builder.done());
        assertEquals("role2", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
        assertEquals("group2", searchedProfileMember.getResult().get(0).getDisplayNamePart2());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member" }, story = "Get number of profile members.")
    @Test
    public void getNumberOfProfileMembers() throws Exception {
        final List<Long> profileIds = new ArrayList<Long>();
        profileIds.add(adminProfileId);
        profileIds.add(userProfileId);
        final Map<Long, Long> numberOfProfileMembers = getProfileAPI().getNumberOfProfileMembers(profileIds);
        assertNotNull(numberOfProfileMembers);
        assertEquals(2, numberOfProfileMembers.size());
        assertEquals(Long.valueOf(5L), numberOfProfileMembers.get(adminProfileId));
        assertEquals(Long.valueOf(1L), numberOfProfileMembers.get(userProfileId));

    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Wrong parameter" }, story = "Execute profile member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = SearchException.class)
    public void searchProfileMembersForProfileWithWrongParameter() throws Exception {
        getProfileAPI().searchProfileMembers("plop", null);
    }

}
