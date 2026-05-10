package com.example.traveling.travelshare.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelShareNotification;
import com.example.traveling.travelshare.domain.TravelShareNotificationRepository;

import java.util.List;

public class TravelShareNotificationsFragment extends Fragment {

    private TravelShareNotificationRepository notificationRepository;
    private LinearLayout notificationsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travelshare_notifications, container, false);

        notificationRepository = TravelShareDataProvider.notificationRepository();
        notificationsContainer = view.findViewById(R.id.notifications_container);

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        notificationsContainer.removeAllViews();
        // Pour ce mock, on suppose que l'utilisateur connecté est "current_user"
        List<TravelShareNotification> notifications = notificationRepository.getNotificationsForUser("current_user");

        if (notifications.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("Aucune notification pour le moment.");
            emptyText.setPadding(16, 16, 16, 16);
            notificationsContainer.addView(emptyText);
            return;
        }

        for (TravelShareNotification notification : notifications) {
            View itemView = getLayoutInflater().inflate(R.layout.item_travelshare_notification, notificationsContainer, false);

            ImageView icon = itemView.findViewById(R.id.notification_icon);
            TextView contentText = itemView.findViewById(R.id.notification_content);
            TextView timeText = itemView.findViewById(R.id.notification_time);
            View unreadIndicator = itemView.findViewById(R.id.notification_unread_indicator);

            // Shape rond pour l'icone
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);

            // Personnalisation selon le type
            switch (notification.getType()) {
                case "LIKE":
                    shape.setColor(Color.parseColor("#E91E63")); // Rose
                    break;
                case "COMMENT":
                    shape.setColor(Color.parseColor("#2196F3")); // Bleu
                    break;
                case "INVITE":
                    shape.setColor(Color.parseColor("#4CAF50")); // Vert
                    break;
                default:
                    shape.setColor(Color.parseColor("#9E9E9E")); // Gris
                    break;
            }
            icon.setBackground(shape);

            contentText.setText(notification.getContent());

            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(notification.getTimestamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            timeText.setText(timeAgo);

            if (!notification.isRead()) {
                unreadIndicator.setVisibility(View.VISIBLE);
                // Rendre le rond bien visible
                GradientDrawable indicatorShape = new GradientDrawable();
                indicatorShape.setShape(GradientDrawable.OVAL);
                indicatorShape.setColor(Color.RED);
                unreadIndicator.setBackground(indicatorShape);
            } else {
                unreadIndicator.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                notificationRepository.markAsRead(notification.getId());
                unreadIndicator.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Notification lue !", Toast.LENGTH_SHORT).show();
            });

            notificationsContainer.addView(itemView);
        }
    }
}
