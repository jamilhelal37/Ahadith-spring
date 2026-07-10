(function () {
    "use strict";

    var initialized = false;
    var inFlight = false;

    function onReady(callback) {
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", callback, { once: true });
        } else {
            callback();
        }
    }

    onReady(function () {
        if (initialized) {
            return;
        }
        initialized = true;

        var title = document.getElementById("verification-title");
        var message = document.getElementById("verification-message");
        var detail = document.getElementById("verification-detail");
        var mark = document.getElementById("status-mark");
        var homeButton = document.getElementById("home-button");
        var params = new URLSearchParams(window.location.search);
        var token = params.get("token");

        window.history.replaceState({}, document.title, window.location.pathname);

        if (!token || !token.trim()) {
            showError("رابط التحقق غير صالح.");
            return;
        }

        verify(token);

        function verify(rawToken) {
            if (inFlight) {
                return;
            }
            inFlight = true;
            showLoading();

            fetch("/auth/verify-email", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "omit",
                body: JSON.stringify({ token: rawToken })
            })
                .then(function (response) {
                    if (response.ok) {
                        showSuccess();
                        return null;
                    }
                    return safeError(response).then(function () {
                        throw new Error(messageForStatus(response.status));
                    });
                })
                .catch(function (error) {
                    showError(error.message || "حدث خطأ أثناء تأكيد البريد الإلكتروني.");
                })
                .finally(function () {
                    inFlight = false;
                });
        }

        function safeError(response) {
            var contentType = response.headers.get("content-type") || "";
            if (!contentType.includes("application/json")) {
                return Promise.resolve({});
            }
            return response.json().catch(function () {
                return {};
            });
        }

        function messageForStatus(status) {
            if (status === 400) {
                return "رابط التحقق غير صالح.";
            }
            if (status === 401 || status === 404) {
                return "لم يتم العثور على طلب التحقق أو انتهت صلاحيته.";
            }
            if (status === 409) {
                return "تم تأكيد البريد الإلكتروني مسبقاً.";
            }
            if (status === 410) {
                return "انتهت صلاحية رابط التحقق.";
            }
            if (status === 429) {
                return "تم إرسال طلبات كثيرة. حاول لاحقاً.";
            }
            return "حدث خطأ أثناء تأكيد البريد الإلكتروني.";
        }

        function showLoading() {
            mark.className = "mark";
            title.textContent = "تأكيد البريد الإلكتروني";
            message.textContent = "جارٍ التحقق من رابط التأكيد...";
            detail.textContent = "";
            homeButton.hidden = true;
        }

        function showSuccess() {
            mark.className = "mark success";
            title.textContent = "تم تأكيد البريد بنجاح";
            message.textContent = "أصبح حسابك فعالاً، ويمكنك الآن تسجيل الدخول.";
            detail.textContent = "";
            homeButton.hidden = false;
        }

        function showError(safeMessage) {
            mark.className = "mark error";
            title.textContent = "تعذر تأكيد البريد";
            message.textContent = safeMessage;
            detail.textContent = "";
            homeButton.hidden = false;
        }
    });
})();
