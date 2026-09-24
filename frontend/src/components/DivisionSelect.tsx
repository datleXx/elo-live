import { NATIONS } from "@/lib/divisions";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectLabel,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface Props {
  value: string;
  onValueChange: (value: string) => void;
  disabled?: boolean;
  className?: string;
}

export function DivisionSelect({ value, onValueChange, disabled, className }: Props) {
  return (
    <Select value={value} onValueChange={onValueChange} disabled={disabled}>
      <SelectTrigger className={className ?? "w-52"}>
        <SelectValue />
      </SelectTrigger>
      <SelectContent>
        {NATIONS.map((nation) => (
          <SelectGroup key={nation.country}>
            <SelectLabel>{nation.country}</SelectLabel>
            {nation.divisions.map((d) => (
              <SelectItem key={d.code} value={d.code}>
                {d.name}
              </SelectItem>
            ))}
          </SelectGroup>
        ))}
      </SelectContent>
    </Select>
  );
}
