# ZenBuddy Report Figures

Thư mục này dùng để đặt ảnh giao diện hoặc sơ đồ trước khi upload lên Overleaf.

Gợi ý tên file:

- `ui-auth.png`
- `ui-onboarding.png`
- `ui-home.png`
- `ui-mood.png`
- `ui-journal.png`
- `ui-chat.png`
- `ui-dashboard.png`
- `ui-steps.png`
- `ui-food.png`
- `ui-exercise.png`
- `ui-profile.png`
- `ui-healthchat.png`
- `ui-schedule.png`
- `ui-settings.png`
- `architecture-overview.png`
- `database-schema.png`

Trong `main.tex`, bạn có thể thay từng `\placeholderfigure` bằng:

```tex
\begin{figure}[H]
  \centering
  \includegraphics[width=0.9\textwidth]{figures/ui-home.png}
  \caption{Màn hình trang chủ của ứng dụng ZenBuddy}
  \label{fig:ui-home}
\end{figure}
```
