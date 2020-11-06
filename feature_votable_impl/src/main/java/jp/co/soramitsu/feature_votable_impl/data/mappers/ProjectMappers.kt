package jp.co.soramitsu.feature_votable_impl.data.mappers

import jp.co.soramitsu.core_db.model.ProjectDetailsLocal
import jp.co.soramitsu.core_db.model.ProjectDetailsWithGalleryLocal
import jp.co.soramitsu.core_db.model.ProjectLocal
import jp.co.soramitsu.core_db.model.ProjectStatusLocal
import jp.co.soramitsu.feature_votable_api.domain.model.project.Project
import jp.co.soramitsu.feature_votable_api.domain.model.project.ProjectDetails
import jp.co.soramitsu.feature_votable_api.domain.model.project.ProjectStatus
import jp.co.soramitsu.feature_votable_impl.data.network.model.ProjectDetailsRemote
import jp.co.soramitsu.feature_votable_impl.data.network.model.ProjectRemote
import java.util.Date

fun mapProjectRemoteToProject(projectRemote: ProjectRemote): Project {
    return with(projectRemote) {
        Project(
            id,
            imageLink,
            projectLink,
            name,
            email ?: "",
            description,
            detailedDescription ?: "",
            Date(fundingDeadline * 1000L),
            fundingCurrent,
            fundingTarget,
            votedFriendsCount,
            favoriteCount,
            votes,
            favorite,
            unwatched,
            ProjectStatus.valueOf(status),
            Date(statusUpdateTime * 1000L)
        )
    }
}

fun mapProjectDetailsRemoteToProject(projectDetailsRemote: ProjectDetailsRemote): ProjectDetails {
    return with(projectDetailsRemote) {
        ProjectDetails(
            id,
            imageLink,
            projectLink,
            name,
            email ?: "",
            description,
            detailedDescription ?: "",
            Date(fundingDeadline * 1000L),
            fundingCurrent,
            fundingTarget,
            votedFriendsCount,
            favoriteCount,
            votes,
            favorite,
            unwatched,
            gallery?.map { mapGalleryRemoteToGallery(it) } ?: emptyList(),
            ProjectStatus.valueOf(status),
            Date(statusUpdateTime * 1000L),
            if (discussionLinkRemote == null) null else mapDiscussionLinkRemoteToDiscussionLink(discussionLinkRemote)
        )
    }
}

fun mapProjectToProjectLocal(project: Project): ProjectLocal {
    return with(project) {
        ProjectLocal(id, image, projectLink, name, email, description, detailedDescription, deadline.time,
            fundingCurrent, fundingTarget, votedFriendsCount, favoriteCount, votes, isFavorite, isUnwatched,
            mapProjectStatusToProjectStatusLocal(status), statusUpdateTime.time)
    }
}

fun mapProjectDetailsToProjectDetailsLocal(project: ProjectDetails): ProjectDetailsLocal {
    return with(project) {
        ProjectDetailsLocal(id, image, projectLink, name, email, description, detailedDescription, deadline.time,
            fundingCurrent, fundingTarget, votedFriendsCount, favoriteCount, votes, isFavorite, isUnwatched,
            mapProjectStatusToProjectStatusLocal(status), statusUpdateTime.time, if (discussionLink == null) null else mapDiscussionLinkToDiscussionLinkLocal(discussionLink!!))
    }
}

fun mapProjectStatusLocalToProjectStatus(projectStatus: ProjectStatusLocal): ProjectStatus {
    return when (projectStatus) {
        ProjectStatusLocal.OPEN -> ProjectStatus.OPEN
        ProjectStatusLocal.FAILED -> ProjectStatus.FAILED
        ProjectStatusLocal.COMPLETED -> ProjectStatus.COMPLETED
    }
}

fun mapProjectStatusToProjectStatusLocal(projectStatus: ProjectStatus): ProjectStatusLocal {
    return when (projectStatus) {
        ProjectStatus.OPEN -> ProjectStatusLocal.OPEN
        ProjectStatus.FAILED -> ProjectStatusLocal.FAILED
        ProjectStatus.COMPLETED -> ProjectStatusLocal.COMPLETED
    }
}

fun mapProjectLocalToProject(projectWithGalleryLocal: ProjectLocal): Project {
    return with(projectWithGalleryLocal) {
        Project(
            id,
            image,
            projectLink,
            name,
            email,
            description,
            detailedDescription,
            Date(deadlineMillis),
            fundingCurrent,
            fundingTarget,
            votedFriendsCount,
            favoriteCount,
            votes,
            isFavorite,
            isUnwatched,
            mapProjectStatusLocalToProjectStatus(status),
            Date(statusUpdateTimeMillis)
        )
    }
}

fun mapProjectDetailsLocalToProjectDetails(projectWithGalleryLocal: ProjectDetailsWithGalleryLocal): ProjectDetails {
    return with(projectWithGalleryLocal) {
        ProjectDetails(
            projectLocal.id,
            projectLocal.image,
            projectLocal.projectLink,
            projectLocal.name,
            projectLocal.email,
            projectLocal.description,
            projectLocal.detailedDescription,
            Date(projectLocal.deadlineMillis),
            projectLocal.fundingCurrent,
            projectLocal.fundingTarget,
            projectLocal.votedFriendsCount,
            projectLocal.favoriteCount,
            projectLocal.votes,
            projectLocal.isFavorite,
            projectLocal.isUnwatched,
            gallery.map { mapGalleryLocalToGallery(it) },
            mapProjectStatusLocalToProjectStatus(projectLocal.status),
            Date(projectLocal.statusUpdateTimeMillis),
            if (projectLocal.discussionLink == null) null else mapDiscussionLinkLocalToDiscussionLink(projectLocal.discussionLink!!)
        )
    }
}