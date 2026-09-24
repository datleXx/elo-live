import { NavLink } from "react-router-dom";
import { Activity, LayoutDashboard, Shield, Trophy, Users } from "lucide-react";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarTrigger,
} from "@/components/ui/sidebar";

const NAV_ITEMS = [
  { to: "/", label: "Live dashboard", icon: LayoutDashboard, end: true },
  { to: "/ratings", label: "Standings", icon: Trophy },
  { to: "/teams", label: "Teams", icon: Users },
  { to: "/admin", label: "Ingest data", icon: Activity },
];

export function AppSidebar() {
  return (
    <Sidebar collapsible="icon">
      <SidebarHeader className="px-3 py-3">
        <div className="flex items-center justify-between gap-2 px-1 group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:px-0">
          <div className="flex items-center gap-2 group-data-[collapsible=icon]:hidden">
            <Shield className="size-5 shrink-0 text-primary" />
            <span className="text-sm font-semibold tracking-tight">Elo Live</span>
          </div>
          <SidebarTrigger />
        </div>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>Browse</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {NAV_ITEMS.map((item) => (
                <SidebarMenuItem key={item.to}>
                  <SidebarMenuButton asChild tooltip={item.label}>
                    <NavLink
                      to={item.to}
                      end={item.end}
                      className={({ isActive }) =>
                        isActive ? "font-medium text-sidebar-accent-foreground" : undefined
                      }
                    >
                      {({ isActive }) => (
                        <span
                          data-active={isActive}
                          className="flex items-center gap-2 data-[active=true]:[&>svg]:text-primary"
                        >
                          <item.icon className="size-4" />
                          <span>{item.label}</span>
                        </span>
                      )}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  );
}
