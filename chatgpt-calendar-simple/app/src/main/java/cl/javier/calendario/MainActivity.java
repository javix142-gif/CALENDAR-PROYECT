package cl.javier.calendario;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final String PREFS = "calendar_prefs";
    private static final String EVENTS_KEY = "events_json";
    private static final int PRIMARY = Color.rgb(63, 81, 181);
    private static final int TEXT = Color.rgb(34, 39, 53);
    private static final int MUTED = Color.rgb(105, 112, 128);
    private static final int BACKGROUND = Color.rgb(246, 247, 251);
    private static final int CARD = Color.WHITE;
    private static final int DANGER = Color.rgb(198, 40, 40);

    private final SimpleDateFormat storageDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat displayDate = new SimpleDateFormat("EEEE d 'de' MMMM", new Locale("es", "CL"));
    private final SimpleDateFormat shortDate = new SimpleDateFormat("EEE d MMM", new Locale("es", "CL"));

    private Calendar selectedDate = Calendar.getInstance();
    private CalendarView calendarView;
    private TextView selectedDateLabel;
    private LinearLayout selectedEventsContainer;
    private LinearLayout upcomingContainer;
    private final List<CalendarEvent> events = new ArrayList<CalendarEvent>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadEvents();
        buildUi();
        refreshAll();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(30));
        scroll.addView(root, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = text("Mi Calendario", 28, TEXT, true);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button today = secondaryButton("Hoy");
        today.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { goToday(); }
        });
        titleRow.addView(today);
        root.addView(titleRow);

        TextView subtitle = text("Organiza tus eventos sin cuenta ni conexión a internet.", 14, MUTED, false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = dp(4);
        subtitleParams.bottomMargin = dp(12);
        root.addView(subtitle, subtitleParams);

        LinearLayout calendarCard = cardContainer();
        calendarView = new CalendarView(this);
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setDate(selectedDate.getTimeInMillis(), false, true);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate.set(year, month, dayOfMonth, 12, 0, 0);
                selectedDate.set(Calendar.MILLISECOND, 0);
                refreshAll();
            }
        });
        calendarCard.addView(calendarView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(calendarCard);

        Button add = primaryButton("+ Nuevo evento");
        add.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showEventDialog(null); }
        });
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        addParams.topMargin = dp(14);
        root.addView(add, addParams);

        selectedDateLabel = text("", 20, TEXT, true);
        LinearLayout.LayoutParams selectedTitleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        selectedTitleParams.topMargin = dp(24);
        root.addView(selectedDateLabel, selectedTitleParams);

        selectedEventsContainer = new LinearLayout(this);
        selectedEventsContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams selectedContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        selectedContainerParams.topMargin = dp(8);
        root.addView(selectedEventsContainer, selectedContainerParams);

        TextView upcomingTitle = text("Próximos eventos", 20, TEXT, true);
        LinearLayout.LayoutParams upcomingTitleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        upcomingTitleParams.topMargin = dp(28);
        root.addView(upcomingTitle, upcomingTitleParams);

        TextView upcomingSubtitle = text("Eventos futuros guardados en este teléfono.", 13, MUTED, false);
        LinearLayout.LayoutParams upcomingSubtitleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        upcomingSubtitleParams.topMargin = dp(2);
        root.addView(upcomingSubtitle, upcomingSubtitleParams);

        upcomingContainer = new LinearLayout(this);
        upcomingContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams upcomingContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        upcomingContainerParams.topMargin = dp(8);
        root.addView(upcomingContainer, upcomingContainerParams);

        setContentView(scroll);
    }

    private void goToday() {
        selectedDate = Calendar.getInstance();
        calendarView.setDate(selectedDate.getTimeInMillis(), true, true);
        refreshAll();
    }

    private void refreshAll() {
        selectedDateLabel.setText(capitalize(displayDate.format(selectedDate.getTime())));
        renderSelectedEvents();
        renderUpcomingEvents();
    }

    private void renderSelectedEvents() {
        selectedEventsContainer.removeAllViews();
        final String key = storageDate.format(selectedDate.getTime());
        List<CalendarEvent> dayEvents = new ArrayList<CalendarEvent>();
        for (CalendarEvent event : events) if (key.equals(event.date)) dayEvents.add(event);
        sortEvents(dayEvents);
        if (dayEvents.isEmpty()) {
            selectedEventsContainer.addView(emptyState("No tienes eventos para este día."));
            return;
        }
        for (CalendarEvent event : dayEvents) selectedEventsContainer.addView(eventCard(event, false));
    }

    private void renderUpcomingEvents() {
        upcomingContainer.removeAllViews();
        Calendar start = Calendar.getInstance();
        normalizeDay(start);
        List<CalendarEvent> upcoming = new ArrayList<CalendarEvent>();
        for (CalendarEvent event : events) {
            Date date = parseDate(event.date);
            if (date != null && !date.before(start.getTime())) upcoming.add(event);
        }
        sortEvents(upcoming);
        if (upcoming.isEmpty()) {
            upcomingContainer.addView(emptyState("No hay eventos futuros."));
            return;
        }
        int limit = Math.min(upcoming.size(), 12);
        for (int i = 0; i < limit; i++) upcomingContainer.addView(eventCard(upcoming.get(i), true));
        if (upcoming.size() > limit) {
            TextView more = text("+ " + (upcoming.size() - limit) + " evento(s) adicional(es)", 13, MUTED, false);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p.topMargin = dp(6);
            upcomingContainer.addView(more, p);
        }
    }

    private View eventCard(final CalendarEvent event, boolean showDate) {
        LinearLayout card = cardContainer();
        card.setPadding(dp(14), dp(12), dp(12), dp(12));
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.TOP);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        top.addView(content, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        content.addView(text(event.title, 16, TEXT, true));
        String meta = event.time;
        if (showDate) {
            Date d = parseDate(event.date);
            if (d != null) meta = capitalize(shortDate.format(d)) + " · " + event.time;
        }
        TextView time = text(meta, 13, PRIMARY, true);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeParams.topMargin = dp(3);
        content.addView(time, timeParams);
        if (event.notes != null && event.notes.trim().length() > 0) {
            TextView notes = text(event.notes.trim(), 13, MUTED, false);
            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            noteParams.topMargin = dp(5);
            content.addView(notes, noteParams);
        }
        Button edit = compactButton("Editar", PRIMARY);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showEventDialog(event); }
        });
        top.addView(edit);
        Button delete = compactButton("Eliminar", DANGER);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { confirmDelete(event); }
        });
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteParams.leftMargin = dp(6);
        top.addView(delete, deleteParams);
        card.addView(top);
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { jumpToEventDate(event); }
        });
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.bottomMargin = dp(8);
        card.setLayoutParams(p);
        return card;
    }

    private void jumpToEventDate(CalendarEvent event) {
        Date d = parseDate(event.date);
        if (d == null) return;
        selectedDate.setTime(d);
        calendarView.setDate(d.getTime(), true, true);
        refreshAll();
    }

    private void showEventDialog(final CalendarEvent existing) {
        final boolean editing = existing != null;
        final Calendar dialogDate = Calendar.getInstance();
        if (editing) {
            Date parsed = parseDate(existing.date);
            if (parsed != null) dialogDate.setTime(parsed);
        } else dialogDate.setTime(selectedDate.getTime());

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22), dp(8), dp(22), 0);
        final EditText title = new EditText(this);
        title.setHint("Título del evento");
        title.setSingleLine(true);
        if (editing) title.setText(existing.title);
        form.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final Button dateButton = secondaryButton("");
        updateDateButton(dateButton, dialogDate);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        dateParams.topMargin = dp(10);
        form.addView(dateButton, dateParams);

        final Calendar timeValue = Calendar.getInstance();
        if (editing) {
            int[] hm = parseTime(existing.time);
            timeValue.set(Calendar.HOUR_OF_DAY, hm[0]);
            timeValue.set(Calendar.MINUTE, hm[1]);
        } else {
            int minute = timeValue.get(Calendar.MINUTE);
            timeValue.set(Calendar.MINUTE, ((minute + 14) / 15) * 15);
            if (timeValue.get(Calendar.MINUTE) >= 60) {
                timeValue.add(Calendar.HOUR_OF_DAY, 1);
                timeValue.set(Calendar.MINUTE, 0);
            }
        }
        final Button timeButton = secondaryButton(formatTime(timeValue));
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        timeParams.topMargin = dp(8);
        form.addView(timeButton, timeParams);

        final EditText notes = new EditText(this);
        notes.setHint("Notas (opcional)");
        notes.setMinLines(2);
        notes.setMaxLines(4);
        notes.setGravity(Gravity.TOP);
        notes.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        if (editing) notes.setText(existing.notes);
        LinearLayout.LayoutParams notesParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        notesParams.topMargin = dp(8);
        form.addView(notes, notesParams);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                DatePickerDialog picker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        dialogDate.set(year, month, dayOfMonth, 12, 0, 0);
                        dialogDate.set(Calendar.MILLISECOND, 0);
                        updateDateButton(dateButton, dialogDate);
                    }
                }, dialogDate.get(Calendar.YEAR), dialogDate.get(Calendar.MONTH), dialogDate.get(Calendar.DAY_OF_MONTH));
                picker.show();
            }
        });
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                TimePickerDialog picker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
                        timeValue.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        timeValue.set(Calendar.MINUTE, minute);
                        timeButton.setText(formatTime(timeValue));
                    }
                }, timeValue.get(Calendar.HOUR_OF_DAY), timeValue.get(Calendar.MINUTE), true);
                picker.show();
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing ? "Editar evento" : "Nuevo evento")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(editing ? "Guardar" : "Crear", null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        String cleanTitle = title.getText().toString().trim();
                        if (cleanTitle.length() == 0) { title.setError("Escribe un título"); return; }
                        if (cleanTitle.length() > 100) { title.setError("Máximo 100 caracteres"); return; }
                        String cleanNotes = notes.getText().toString().trim();
                        if (cleanNotes.length() > 500) { notes.setError("Máximo 500 caracteres"); return; }
                        if (editing) {
                            existing.title = cleanTitle;
                            existing.date = storageDate.format(dialogDate.getTime());
                            existing.time = formatTime(timeValue);
                            existing.notes = cleanNotes;
                        } else {
                            CalendarEvent event = new CalendarEvent();
                            event.id = UUID.randomUUID().toString();
                            event.title = cleanTitle;
                            event.date = storageDate.format(dialogDate.getTime());
                            event.time = formatTime(timeValue);
                            event.notes = cleanNotes;
                            events.add(event);
                        }
                        saveEvents();
                        selectedDate.setTime(dialogDate.getTime());
                        calendarView.setDate(dialogDate.getTimeInMillis(), true, true);
                        refreshAll();
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    private void confirmDelete(final CalendarEvent event) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar evento")
                .setMessage("¿Eliminar \"" + event.title + "\"?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        events.remove(event);
                        saveEvents();
                        refreshAll();
                        Toast.makeText(MainActivity.this, "Evento eliminado", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void loadEvents() {
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(EVENTS_KEY, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                CalendarEvent event = new CalendarEvent();
                event.id = obj.optString("id", UUID.randomUUID().toString());
                event.title = obj.optString("title", "Sin título");
                event.date = obj.optString("date", storageDate.format(new Date()));
                event.time = obj.optString("time", "09:00");
                event.notes = obj.optString("notes", "");
                events.add(event);
            }
        } catch (JSONException ignored) { events.clear(); }
    }

    private void saveEvents() {
        JSONArray array = new JSONArray();
        for (CalendarEvent event : events) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", event.id);
                obj.put("title", event.title);
                obj.put("date", event.date);
                obj.put("time", event.time);
                obj.put("notes", event.notes == null ? "" : event.notes);
                array.put(obj);
            } catch (JSONException ignored) { }
        }
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(EVENTS_KEY, array.toString()).apply();
    }

    private void sortEvents(List<CalendarEvent> list) {
        Collections.sort(list, new Comparator<CalendarEvent>() {
            @Override public int compare(CalendarEvent a, CalendarEvent b) {
                int byDate = a.date.compareTo(b.date);
                if (byDate != 0) return byDate;
                int byTime = a.time.compareTo(b.time);
                if (byTime != 0) return byTime;
                return a.title.compareToIgnoreCase(b.title);
            }
        });
    }

    private Date parseDate(String date) {
        try { return storageDate.parse(date); } catch (ParseException e) { return null; }
    }
    private int[] parseTime(String time) {
        int[] fallback = new int[] {9, 0};
        if (time == null) return fallback;
        String[] parts = time.split(":");
        if (parts.length != 2) return fallback;
        try {
            int h = Integer.parseInt(parts[0]), m = Integer.parseInt(parts[1]);
            if (h < 0 || h > 23 || m < 0 || m > 59) return fallback;
            return new int[] {h, m};
        } catch (NumberFormatException e) { return fallback; }
    }
    private void normalizeDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
    }
    private String formatTime(Calendar c) {
        return String.format(Locale.US, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }
    private void updateDateButton(Button button, Calendar date) {
        button.setText("Fecha: " + capitalize(displayDate.format(date.getTime())));
    }
    private String capitalize(String value) {
        if (value == null || value.length() == 0) return value;
        return value.substring(0, 1).toUpperCase(new Locale("es", "CL")) + value.substring(1);
    }
    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(sp); view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }
    private TextView emptyState(String value) {
        TextView view = text(value, 14, MUTED, false);
        view.setGravity(Gravity.CENTER); view.setPadding(dp(14), dp(18), dp(14), dp(18));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE); bg.setCornerRadius(dp(14)); bg.setStroke(dp(1), Color.rgb(226, 229, 238));
        view.setBackground(bg);
        return view;
    }
    private LinearLayout cardContainer() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(CARD); bg.setCornerRadius(dp(16)); bg.setStroke(dp(1), Color.rgb(228, 231, 239));
        layout.setBackground(bg); layout.setElevation(dp(1));
        return layout;
    }
    private Button primaryButton(String label) {
        Button button = new Button(this);
        button.setText(label); button.setTextColor(Color.WHITE); button.setTextSize(15); button.setAllCaps(false); button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(PRIMARY); bg.setCornerRadius(dp(14)); button.setBackground(bg);
        return button;
    }
    private Button secondaryButton(String label) {
        Button button = new Button(this);
        button.setText(label); button.setTextColor(PRIMARY); button.setTextSize(14); button.setAllCaps(false);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.WHITE); bg.setCornerRadius(dp(12)); bg.setStroke(dp(1), Color.rgb(208, 214, 232)); button.setBackground(bg);
        return button;
    }
    private Button compactButton(String label, int color) {
        Button button = new Button(this);
        button.setText(label); button.setTextColor(color); button.setTextSize(12); button.setAllCaps(false); button.setPadding(dp(8), 0, dp(8), 0);
        button.setMinHeight(0); button.setMinimumHeight(0); button.setMinWidth(0); button.setMinimumWidth(0);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.rgb(248, 249, 252)); bg.setCornerRadius(dp(10)); bg.setStroke(dp(1), Color.rgb(222, 225, 234)); button.setBackground(bg);
        return button;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static class CalendarEvent { String id; String title; String date; String time; String notes; }
}
