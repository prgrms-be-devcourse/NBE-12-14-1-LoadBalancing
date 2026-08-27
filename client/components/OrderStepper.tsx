const STEPS = ["메뉴 선택", "주문확인", "결제하기", "결제 완료"];

export default function OrderStepper({ currentStep }: { currentStep: number }) {
  return (
    <div className="mb-8 flex items-center justify-center gap-2">
      {STEPS.map((label, index) => {
        const step = index + 1;
        const isActive = step === currentStep;
        const isDone = step < currentStep;

        return (
          <div key={label} className="flex items-center gap-2">
            <div className="flex items-center gap-1.5">
              <span
                className={`flex h-6 w-6 items-center justify-center rounded-full text-xs font-medium ${
                  isActive
                    ? "bg-black text-white"
                    : isDone
                      ? "bg-gray-300 text-white"
                      : "bg-gray-100 text-gray-400"
                }`}
              >
                {step}
              </span>
              <span
                className={`text-sm ${
                  isActive ? "font-medium text-black" : "text-gray-400"
                }`}
              >
                {label}
              </span>
            </div>
            {step < STEPS.length && <span className="h-px w-6 bg-gray-200" />}
          </div>
        );
      })}
    </div>
  );
}
