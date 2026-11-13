package com.example.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import com.example.data.model.ReelModerationStage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.ReelCommentItem
import com.example.data.model.ReelItem
import com.example.data.model.SocialUserProfile
import com.example.ui.components.copyTextToClipboard
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulGoldStar
import com.example.ui.theme.FoulLiveGreen
import com.example.ui.theme.FoulLiveRed
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurplePrimary
import com.example.ui.theme.FoulTextSecondary

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

private val FoulGoldAccent = FoulGoldStar
private val FoulPurpleSoft = Color(0xFFB388FF)

private data class InspectedUserSeed(
    val userId: String,
    val personalName: String,
    val userHandle: String,
    val userPhotoUrl: String
)

@Composable
fun ReelsScreen(
    reels: List<ReelItem>,
    allReels: List<ReelItem> = reels,
    currentUserId: String = "",
    followingUserIds: Set<String> = emptySet(),
    isLoading: Boolean,
    isUploading: Boolean,
    moderationStage: ReelModerationStage = ReelModerationStage.IDLE,
    uploadStatusMessage: String?,
    selectedReelIdFromDeepLink: String?,
    officialChannelUrl: String,
    commentsForActiveReel: List<ReelCommentItem>,
    isLoadingComments: Boolean,
    onRefreshReels: () -> Unit,
    onUploadReelVideo: (Uri, String, String) -> Unit,
    onToggleLike: (ReelItem) -> Unit,
    onRecordView: (ReelItem) -> Unit,
    onRecordShare: (ReelItem) -> Unit,
    onOpenComments: (ReelItem) -> Unit,
    onAddComment: (ReelItem, String) -> Unit,
    onDeleteReel: (ReelItem) -> Unit = {},
    onToggleFollowUser: (String) -> Unit = {},
    onFetchPublicUserProfile: (suspend (String, String, String, String) -> SocialUserProfile)? = null,
    onConsumeDeepLinkReelId: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isFullScreenPublishOpen by remember { mutableStateOf(false) }
    var activeCommentsReel by remember { mutableStateOf<ReelItem?>(null) }
    var activeOptionsReel by remember { mutableStateOf<ReelItem?>(null) }
    var showPublishAccountRequiredDialog by remember { mutableStateOf(false) }
    var isAnyEdgeFastForwarding by remember { mutableStateOf(false) }
    var inspectedUserSeed by remember { mutableStateOf<InspectedUserSeed?>(null) }

    // Automatically close full-screen publish page when upload succeeds
    LaunchedEffect(isUploading, uploadStatusMessage) {
        if (!isUploading && isFullScreenPublishOpen && uploadStatusMessage != null) {
            if (uploadStatusMessage.contains("تم رفع ونشر") || uploadStatusMessage.contains("بنجاح")) {
                isFullScreenPublishOpen = false
            }
        }
    }

    val triggerPublishReel: () -> Unit = {
        if (!com.example.data.repository.SocialFirebaseRepository.isUserAuthenticatedWithCompleteProfile()) {
            showPublishAccountRequiredDialog = true
        } else {
            isFullScreenPublishOpen = true
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { reels.size.coerceAtLeast(1) }
    )

    // Scroll to deep-linked Reel if present
    LaunchedEffect(selectedReelIdFromDeepLink, reels.size) {
        val targetId = selectedReelIdFromDeepLink?.trim().orEmpty()
        if (targetId.isNotEmpty() && reels.isNotEmpty()) {
            val idx = reels.indexOfFirst {
                it.id.equals(targetId, ignoreCase = true) ||
                    "${it.chatId}_${it.messageId}".equals(targetId, ignoreCase = true)
            }
            if (idx >= 0) {
                pagerState.scrollToPage(idx)
                onConsumeDeepLinkReelId()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("reels_screen_root")
    ) {
        if (isLoading && reels.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    text = "جاري تحميل مقاطع FOUL REELS...",
                    color = FoulTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (reels.isEmpty()) {
            EmptyReelsState(
                officialChannelUrl = officialChannelUrl,
                onPublishClick = triggerPublishReel,
                onRefreshClick = onRefreshReels
            )
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("reels_vertical_pager"),
                beyondViewportPageCount = 1,
                key = { page -> reels.getOrNull(page)?.id ?: "empty_$page" }
            ) { page ->
                val reel = reels.getOrNull(page)
                if (reel != null) {
                    val isCurrentPage = pagerState.settledPage == page
                    ReelPagePlayerItem(
                        reel = reel,
                        currentUserId = currentUserId,
                        isFollowingCreator = followingUserIds.contains(reel.userId),
                        isCurrentPage = isCurrentPage,
                        onLikeClick = { onToggleLike(reel) },
                        onCommentClick = {
                            activeCommentsReel = reel
                            onOpenComments(reel)
                        },
                        onViewRecorded = { onRecordView(reel) },
                        onOpenLongPressOptions = {
                            activeOptionsReel = reel
                        },
                        onToggleFollowCreator = {
                            if (reel.userId.isNotBlank()) {
                                onToggleFollowUser(reel.userId)
                            }
                        },
                        onOpenCreatorProfile = {
                            inspectedUserSeed = InspectedUserSeed(
                                userId = reel.userId.ifBlank { "foul_user" },
                                personalName = reel.userPersonalName.ifBlank { reel.userName.ifBlank { "مستخدم FOUL TV" } },
                                userHandle = reel.userHandle.ifBlank { "@foultv" },
                                userPhotoUrl = reel.userPhotoUrl.ifBlank { SocialUserProfile.DEFAULT_PROFILE_PHOTO_URL }
                            )
                        },
                        onEdgeFastForwardChanged = { active ->
                            isAnyEdgeFastForwarding = active
                        }
                    )
                }
            }
        }

        // Top Overlay Bar (hidden while pressing screen edges for 2x speed)
        AnimatedVisibility(
            visible = !isAnyEdgeFastForwarding,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pure '+' Publish Icon Button in high-contrast White & Black ("بشكل فقط زائد بلون أسود أو أبيض حسب البارز")
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, Color.Black.copy(alpha = 0.35f), CircleShape)
                        .clickable(onClick = triggerPublishReel)
                        .testTag("publish_reel_top_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "نشر ريلز",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onRefreshReels,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.62f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                        .testTag("reels_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث الريلز",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // Upload progress banner if uploading
        AnimatedVisibility(
            visible = isUploading || !uploadStatusMessage.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 62.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0A0A0E).copy(alpha = 0.96f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = uploadStatusMessage ?: "جاري رفع ونشر الريلز على FOUL TV...",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Full-Screen Professional Video Publish Screen ("فتح صفحة بملء كامل واحترافية جداً ومنظمة")
    if (isFullScreenPublishOpen) {
        ReelsPublishFullScreen(
            isUploading = isUploading,
            moderationStage = moderationStage,
            externalStatusMessage = uploadStatusMessage,
            onPublishVideo = { videoUri, caption ->
                onUploadReelVideo(videoUri, caption, caption)
            },
            onClose = {
                if (!isUploading) {
                    isFullScreenPublishOpen = false
                }
            }
        )
    }

    // Bottom-to-Half-Screen Black & White Comments Sheet ("فتح من تحت إلى المنتصف وبترتيب أفضل جدا وفقط أسود وأبيض قليل")
    if (activeCommentsReel != null) {
        ReelCommentsDialog(
            reel = activeCommentsReel!!,
            comments = commentsForActiveReel,
            isLoading = isLoadingComments,
            onDismiss = { activeCommentsReel = null },
            onOpenUserProfile = { comment ->
                inspectedUserSeed = InspectedUserSeed(
                    userId = comment.userId.ifBlank { "foul_user" },
                    personalName = comment.userPersonalName.ifBlank { comment.userName.ifBlank { "مشاهد FOUL TV" } },
                    userHandle = comment.userHandle.ifBlank { "@${comment.userName.ifBlank { "foultv" }}" },
                    userPhotoUrl = comment.userPhotoUrl.ifBlank { SocialUserProfile.DEFAULT_PROFILE_PHOTO_URL }
                )
            },
            onSendComment = { commentText ->
                onAddComment(activeCommentsReel!!, commentText)
            }
        )
    }

    // Public User Profile Dialog (opened from Reel Creator OR Comments)
    if (inspectedUserSeed != null) {
        val seed = inspectedUserSeed!!
        val combinedReels = remember(reels, allReels) {
            (allReels + reels).distinctBy { it.id }
        }
        PublicUserProfileDialog(
            seed = seed,
            currentUserId = currentUserId,
            isFollowing = followingUserIds.contains(seed.userId),
            allReels = combinedReels,
            onFetchProfile = onFetchPublicUserProfile,
            onToggleFollow = {
                if (seed.userId.isNotBlank()) {
                    onToggleFollowUser(seed.userId)
                }
            },
            onSelectReel = { selectedReel ->
                inspectedUserSeed = null
                activeCommentsReel = null
                val targetIndex = reels.indexOfFirst { it.id == selectedReel.id }
                if (targetIndex >= 0) {
                    coroutineScope.launch {
                        pagerState.scrollToPage(targetIndex)
                    }
                }
            },
            onDismiss = { inspectedUserSeed = null }
        )
    }

    // Long-Press Options Dialog:
    // 1. "نسخ رابط الفيديو" (Smart Share Link + Caption + Official Telegram Channel URL ONLY)
    // 2. "حذف الريلز" (Visible ONLY to the owner who published the Reel)
    if (activeOptionsReel != null) {
        val targetReel = activeOptionsReel!!
        val isOwner = currentUserId.isNotBlank() && targetReel.userId == currentUserId
        ReelLongPressOptionsDialog(
            reel = targetReel,
            isOwner = isOwner,
            officialChannelUrl = officialChannelUrl,
            onDismiss = { activeOptionsReel = null },
            onCopySmartVideoLink = {
                onRecordShare(targetReel)
                val telegramUrl = targetReel.channelUrl.ifBlank { officialChannelUrl }
                val smartCopyText = buildString {
                    append("🎬 شاهد هذا الفيديو على تطبيق FOUL TV:\n")
                    if (targetReel.caption.isNotBlank()) {
                        append("${targetReel.caption}\n\n")
                    }
                    append("🔗 رابط الفيديو الذكي:\n")
                    append("${targetReel.shareUrl}\n\n")
                    append("📢 قناتنا الرسمية على تيلجرام:\n")
                    append(telegramUrl)
                }
                copyTextToClipboard(
                    context = context,
                    label = "FOUL Smart Video Link",
                    text = smartCopyText,
                    toastMessage = "تم نسخ رابط الفيديو الذكي مع رابط قناة التيلجرام!"
                )
                activeOptionsReel = null
            },
            onDeleteReel = {
                activeOptionsReel = null
                onDeleteReel(targetReel)
            }
        )
    }

    // Account Required Dialog when a Guest/Skipped user tries to Publish a Reel
    if (showPublishAccountRequiredDialog) {
        Dialog(onDismissRequest = { showPublishAccountRequiredDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0A0A0E),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "نشر الريلز يتطلب إنشاء حساب",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "لقد اخترت تخطي إنشاء الحساب عند الدخول. لا يمكنك نشر مقاطع ريلز أو نسخ أي رابط فيديو أو قنوات إلا بعد إكمال المطلوب وإنشاء حساب أو تسجيل الدخول.",
                        color = FoulTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            showPublishAccountRequiredDialog = false
                            com.example.data.repository.SocialFirebaseRepository.requestOpenFullAuthScreen()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FoulPurplePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reels_publish_locked_create_account_button")
                    ) {
                        Text(
                            text = "إنشاء حساب / تسجيل الدخول الآن",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                    Surface(
                        onClick = { showPublishAccountRequiredDialog = false },
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.06f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "إغلاق",
                                color = FoulTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ReelPagePlayerItem(
    reel: ReelItem,
    currentUserId: String,
    isFollowingCreator: Boolean,
    isCurrentPage: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onViewRecorded: () -> Unit,
    onOpenLongPressOptions: () -> Unit,
    onToggleFollowCreator: () -> Unit,
    onOpenCreatorProfile: () -> Unit,
    onEdgeFastForwardChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPausedManually by remember(reel.id) { mutableStateOf(false) }
    var isBuffering by remember(reel.id) { mutableStateOf(true) }
    var hasRecordedView by remember(reel.id) { mutableStateOf(false) }
    var usedFallbackUrl by remember(reel.id) { mutableStateOf(false) }
    var isFastForward2x by remember(reel.id) { mutableStateOf(false) }

    val exoPlayer = remember(reel.id) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("FOUL-TV-ExoPlayer/1.2.2")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(20_000)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1_500,
                15_000,
                500,
                1_000
            )
            .build()

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpDataSourceFactory))
            .setLoadControl(loadControl)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                val initialUrl = reel.streamUrl.ifBlank { reel.videoUrl }
                if (initialUrl.isNotBlank()) {
                    setMediaItem(MediaItem.fromUri(initialUrl))
                    prepare()
                }
            }
    }

    LaunchedEffect(isFastForward2x) {
        exoPlayer.setPlaybackSpeed(if (isFastForward2x) 2.0f else 1.0f)
        onEdgeFastForwardChanged(isFastForward2x)
    }

    DisposableEffect(exoPlayer, reel.id) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY && isCurrentPage && !hasRecordedView) {
                    hasRecordedView = true
                    onViewRecorded()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                if (!usedFallbackUrl && reel.videoUrl.isNotBlank() && reel.videoUrl != reel.streamUrl) {
                    usedFallbackUrl = true
                    exoPlayer.setMediaItem(MediaItem.fromUri(reel.videoUrl))
                    exoPlayer.prepare()
                    if (isCurrentPage && !isPausedManually) {
                        exoPlayer.playWhenReady = true
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            onEdgeFastForwardChanged(false)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isCurrentPage, isPausedManually) {
        if (isCurrentPage && !isPausedManually) {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        } else {
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
        }
    }

    DisposableEffect(lifecycleOwner, exoPlayer, isCurrentPage) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> {
                    if (isCurrentPage && !isPausedManually) {
                        exoPlayer.play()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(reel.id) {
                detectTapGestures(
                    onPress = { offset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val isEdgeTouch = offset.x < w * 0.24f || offset.x > w * 0.76f
                        if (isEdgeTouch) {
                            isFastForward2x = true
                            try {
                                tryAwaitRelease()
                            } finally {
                                isFastForward2x = false
                            }
                        }
                    },
                    onLongPress = { offset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val isCenterTouch = offset.x in (w * 0.24f)..(w * 0.76f)
                        if (isCenterTouch) {
                            onOpenLongPressOptions()
                        }
                    },
                    onTap = { offset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val isCenterTouch = offset.x in (w * 0.24f)..(w * 0.76f)
                        if (isCenterTouch) {
                            isPausedManually = !isPausedManually
                        }
                    }
                )
            }
    ) {
        // Google Media3 ExoPlayer Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    player = exoPlayer
                }
            },
            update = { view ->
                if (view.player !== exoPlayer) {
                    view.player = exoPlayer
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("reel_exoplayer_view_${reel.id}")
        )

        // Subtle 2x Fast-Forward Pill Indicator when holding screen edges
        AnimatedVisibility(
            visible = isFastForward2x,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 18.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "تسريع الفيديو 2x",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Center Play icon indicator when paused manually
        AnimatedVisibility(
            visible = isPausedManually && !isFastForward2x,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "تشغيل",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        if (isBuffering && isCurrentPage) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.Center)
            )
        }

        // Overlay UI (Hidden automatically when pressing screen edges for 2x speed)
        AnimatedVisibility(
            visible = !isFastForward2x,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Bottom gradient scrim for readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.50f),
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                // Right-Side Interactive Action Rail: Like + Comment + Share
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 14.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    ReelActionCircleButton(
                        icon = if (reel.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        iconTint = if (reel.isLikedByMe) Color(0xFFFF1744) else Color.White,
                        label = reel.likesCount.toString(),
                        testTag = "reel_like_button_${reel.id}",
                        onClick = onLikeClick
                    )

                    ReelActionCircleButton(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        iconTint = Color.White,
                        label = reel.commentsCount.toString(),
                        testTag = "reel_comment_button_${reel.id}",
                        onClick = onCommentClick
                    )

                    ReelActionCircleButton(
                        icon = Icons.Default.Share,
                        iconTint = Color.White,
                        label = "مشاركة",
                        testTag = "reel_options_button_${reel.id}",
                        onClick = onOpenLongPressOptions
                    )
                }

                // Bottom-Start Creator Info (Clickable to open Creator's Public Profile!) & Caption
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.78f)
                        .padding(start = 16.dp, end = 12.dp, bottom = 26.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Clickable Creator Avatar + Name + @handle ("إمكانية رؤية حساب الشخصي لصاحب الريلز")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable(onClick = onOpenCreatorProfile)
                                .padding(end = 4.dp)
                                .testTag("reel_creator_profile_trigger_${reel.id}")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF121218),
                                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.85f)),
                                modifier = Modifier.size(42.dp)
                            ) {
                                AsyncImage(
                                    model = reel.userPhotoUrl.ifBlank { SocialUserProfile.DEFAULT_PROFILE_PHOTO_URL },
                                    contentDescription = reel.userPersonalName.ifBlank { reel.userName },
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = reel.userPersonalName.ifBlank { reel.userName.ifBlank { "مستخدم FOUL TV" } },
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (reel.userHandle.startsWith("@")) reel.userHandle else "@${reel.userHandle.ifBlank { "foultv" }}",
                                    color = Color.White.copy(alpha = 0.72f),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Follow / Following Pill if not current user
                        if (reel.userId.isNotBlank() && reel.userId != currentUserId && reel.userId != "foul_user") {
                            Surface(
                                onClick = onToggleFollowCreator,
                                shape = RoundedCornerShape(999.dp),
                                color = if (isFollowingCreator) {
                                    Color.White.copy(alpha = 0.14f)
                                } else {
                                    Color.White
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (isFollowingCreator) Color.White.copy(alpha = 0.35f) else Color.White
                                ),
                                modifier = Modifier.testTag("reel_follow_creator_button_${reel.id}")
                            ) {
                                Text(
                                    text = if (isFollowingCreator) "تتابعه" else "متابعة",
                                    color = if (isFollowingCreator) Color.White else Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (reel.caption.isNotBlank()) {
                        Text(
                            text = reel.caption,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Long-Press Options Dialog for a Reel:
 * - "نسخ رابط الفيديو": Copies ONLY the Smart Share Link formatted with caption and Official Telegram Channel URL.
 * - "حذف الريلز": Displayed strictly ONLY to the publisher who owns the Reel (`isOwner == true`).
 */
@Composable
private fun ReelLongPressOptionsDialog(
    reel: ReelItem,
    isOwner: Boolean,
    officialChannelUrl: String,
    onDismiss: () -> Unit,
    onCopySmartVideoLink: () -> Unit,
    onDeleteReel: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0A0A0E),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reel_long_press_options_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "خيارات المقطع",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = FoulTextSecondary
                        )
                    }
                }

                // Option 1: نسخ رابط الفيديو
                Surface(
                    onClick = onCopySmartVideoLink,
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF14141C),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reel_option_copy_smart_video_link")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "نسخ رابط الفيديو",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.5.sp
                            )
                            Text(
                                text = "نسخ الرابط الذكي للمقطع مع رابط قناة التيلجرام الرسمية (${officialChannelUrl.removePrefix("https://")})",
                                color = FoulTextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                // Option 2: حذف الريلز (Visible ONLY if the current user published this Reel)
                if (isOwner) {
                    Surface(
                        onClick = onDeleteReel,
                        shape = RoundedCornerShape(16.dp),
                        color = FoulLiveRed.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, FoulLiveRed.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reel_option_delete_own_reel")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = Color(0xFFFF6E6E),
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "حذف الريلز",
                                    color = Color(0xFFFF8A80),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp
                                )
                                Text(
                                    text = "حذف هذا المقطع الذي قمت بنشره نهائياً",
                                    color = FoulTextSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReelActionCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.56f))
                .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(23.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun EmptyReelsState(
    officialChannelUrl: String,
    onPublishClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF121218),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
            modifier = Modifier.size(84.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "مرحباً بك في قسم FOUL REELS",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "متصل بقاعدة بيانات FOUL ومشغل Google Media3 ExoPlayer. يمكنك نشر أول مقطع ريلز من هاتفك الآن أو تحديث القائمة.",
            color = FoulTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(22.dp))
        Button(
            onClick = onPublishClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("empty_reels_publish_button")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "نشر مقطع ريلز من الهاتف",
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = onRefreshClick,
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF101016),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "تحديث المقاطع",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            Surface(
                onClick = {
                    copyTextToClipboard(
                        context = context,
                        label = "FOUL Official Channel",
                        text = officialChannelUrl,
                        toastMessage = "تم نسخ رابط القناة الرسمية!"
                    )
                },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF101016),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "نسخ رابط القناة",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Bottom-to-Half-Screen Comments Sheet ("فتح من تحت إلى المنتصف وبترتيب أفضل جدا وفقط أسود وأبيض قليل"):
 * - Opens anchored at the bottom taking 52% of screen height (`fillMaxHeight(0.52f)`).
 * - Pure sleek Black (`#060608`, `#111116`) & subtle White (`#FFFFFF`, `#9A9AA6`) aesthetic.
 * - Clicking any commenter's avatar or name opens their Public User Profile (`onOpenUserProfile`).
 */
@Composable
private fun ReelCommentsDialog(
    reel: ReelItem,
    comments: List<ReelCommentItem>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onOpenUserProfile: (ReelCommentItem) -> Unit,
    onSendComment: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.52f))
                .clickable(onClick = onDismiss)
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color(0xFF060608),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.14f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f)
                    .clickable(enabled = false) {}
                    .testTag("reel_comments_bottom_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Top drag handle bar in subtle white
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(38.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "التعليقات (${comments.size.coerceAtLeast(reel.commentsCount)})",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.09f), thickness = 0.7.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Comments list
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (isLoading && comments.isEmpty()) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.Center)
                            )
                        } else if (comments.isEmpty()) {
                            Text(
                                text = "لا توجد تعليقات بعد. كن أول من يعلق!",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(comments, key = { it.id }) { comment ->
                                    val displayPersonal = comment.userPersonalName.ifBlank { comment.userName.ifBlank { "مشاهد FOUL TV" } }
                                    val displayHandle = if (comment.userHandle.startsWith("@")) {
                                        comment.userHandle
                                    } else {
                                        "@${comment.userHandle.ifBlank { comment.userName.ifBlank { "foultv" } }}"
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Clickable commenter avatar
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF14141A),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable { onOpenUserProfile(comment) }
                                                .testTag("comment_user_avatar_${comment.id}")
                                        ) {
                                            AsyncImage(
                                                model = comment.userPhotoUrl.ifBlank { SocialUserProfile.DEFAULT_PROFILE_PHOTO_URL },
                                                contentDescription = displayPersonal,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { onOpenUserProfile(comment) }
                                                    .testTag("comment_user_name_${comment.id}")
                                            ) {
                                                Text(
                                                    text = displayPersonal,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = displayHandle,
                                                    color = Color.White.copy(alpha = 0.50f),
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = comment.text,
                                                color = Color.White.copy(alpha = 0.90f),
                                                fontSize = 13.5.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.09f), thickness = 0.7.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Minimalist Black & White Comment Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = "أضف تعليقاً...",
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(999.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF111116),
                                unfocusedContainerColor = Color(0xFF111116),
                                focusedBorderColor = Color.White.copy(alpha = 0.45f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reel_comment_input")
                        )
                        IconButton(
                            onClick = {
                                val trimmed = inputText.trim()
                                if (trimmed.isNotEmpty()) {
                                    onSendComment(trimmed)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .testTag("reel_comment_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال التعليق",
                                tint = Color.Black,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full Public User Profile Page (`إمكانية الدخول للحسابات الأشخاص وإمكانية فتح الحسابات وعرض بشكل صفحة عرض جميلة ومرتبة`)
 * - Can be opened from both the Reel Creator header AND any Commenter in the Comments Sheet.
 * - Shows Profile Photo, Personal Name, `@username`, Follow/Following button, Posts/Followers/Following counters,
 *   and a 3-column grid of all Reels published by that user.
 */
@Composable
private fun PublicUserProfileDialog(
    seed: InspectedUserSeed,
    currentUserId: String,
    isFollowing: Boolean,
    allReels: List<ReelItem>,
    onFetchProfile: (suspend (String, String, String, String) -> SocialUserProfile)?,
    onToggleFollow: () -> Unit,
    onSelectReel: (ReelItem) -> Unit,
    onDismiss: () -> Unit
) {
    var loadedProfile by remember(seed) {
        mutableStateOf(
            SocialUserProfile(
                uid = seed.userId,
                email = "",
                username = seed.userHandle.removePrefix("@"),
                avatarId = "avatar_1",
                personalName = seed.personalName,
                profilePhotoUrl = seed.userPhotoUrl,
                isProfileComplete = true,
                hasCompletedPhotoStep = true
            )
        )
    }

    LaunchedEffect(seed) {
        if (onFetchProfile != null) {
            val fetched = runCatching {
                onFetchProfile(
                    seed.userId,
                    seed.personalName,
                    seed.userHandle,
                    seed.userPhotoUrl
                )
            }.getOrNull()
            if (fetched != null) {
                loadedProfile = fetched
            }
        }
    }

    val cleanSeedHandle = seed.userHandle.removePrefix("@").lowercase()
    val userReels = remember(allReels, seed, loadedProfile) {
        allReels.filter { r ->
            (seed.userId.isNotBlank() && seed.userId != "foul_user" && r.userId == seed.userId) ||
                (cleanSeedHandle.isNotBlank() && r.userHandle.removePrefix("@").lowercase() == cleanSeedHandle)
        }.sortedByDescending { it.createdAt }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            color = Color(0xFF040406),
            modifier = Modifier
                .fillMaxSize()
                .testTag("public_user_profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF121218))
                            .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                            .testTag("close_public_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Text(
                        text = "@${loadedProfile.username.removePrefix("@").ifBlank { "foultv" }}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.size(38.dp))
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.7.dp)

                // Profile Info Header (Instagram-style layout)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Profile Photo
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF121218),
                            border = BorderStroke(1.8.dp, Color.White.copy(alpha = 0.85f)),
                            modifier = Modifier.size(82.dp)
                        ) {
                            AsyncImage(
                                model = loadedProfile.profilePhotoUrl.ifBlank { SocialUserProfile.DEFAULT_PROFILE_PHOTO_URL },
                                contentDescription = loadedProfile.personalName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        // 3 Counters: المنشورات | المتابِعون | يتابع
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PublicProfileStatItem(count = userReels.size, label = "المنشورات")
                            PublicProfileStatItem(
                                count = (loadedProfile.followersCount + if (isFollowing) 1 else 0).coerceAtLeast(0),
                                label = "المتابِعون"
                            )
                            PublicProfileStatItem(count = loadedProfile.followingCount, label = "يتابع")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Personal Name & @handle aligned to Start
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = loadedProfile.personalName.ifBlank { loadedProfile.username.ifBlank { "مستخدم FOUL TV" } },
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "@${loadedProfile.username.removePrefix("@").ifBlank { "foultv" }}",
                            color = Color.White.copy(alpha = 0.58f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }

                    // Follow / Following Action Button
                    if (seed.userId.isNotBlank() && seed.userId != currentUserId && seed.userId != "foul_user") {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            onClick = onToggleFollow,
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFollowing) Color(0xFF14141C) else Color.White,
                            border = BorderStroke(
                                1.dp,
                                if (isFollowing) Color.White.copy(alpha = 0.25f) else Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("public_profile_follow_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAddAlt1,
                                    contentDescription = null,
                                    tint = if (isFollowing) Color.White else Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isFollowing) "تتابعه" else "متابعة الحساب",
                                    color = if (isFollowing) Color.White else Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Divider between Profile Header and Reels Grid
                HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.8.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF08080C))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "المنشورات (${userReels.size})",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.6.dp)

                // 3-Column Grid of User's Reels
                if (userReels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد مقاطع ريلز منشورة لهذا الحساب بعد.",
                            color = Color.White.copy(alpha = 0.50f),
                            fontSize = 13.5.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(userReels, key = { it.id }) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(9f / 14f)
                                    .background(Color(0xFF0E0E14))
                                    .clickable { onSelectReel(item) }
                                    .testTag("public_profile_reel_${item.id}")
                            ) {
                                AsyncImage(
                                    model = item.thumbnailUrl.ifBlank { item.userPhotoUrl.ifBlank { SocialUserProfile.DEFAULT_PROFILE_PHOTO_URL } },
                                    contentDescription = item.caption,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.72f)
                                                )
                                            )
                                        )
                                )
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = item.viewsCount.toString(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicProfileStatItem(
    count: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.65f),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

