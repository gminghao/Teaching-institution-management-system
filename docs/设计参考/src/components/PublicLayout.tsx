import { Link, Outlet, useLocation } from "react-router-dom";
import { GraduationCap } from "lucide-react";
import { cn } from "../lib/utils";

export default function PublicLayout() {
  const location = useLocation();

  return (
    <div className="bg-background text-foreground min-h-screen flex flex-col font-sans">
      <nav className="fixed top-0 w-full z-50 bg-surface/80 backdrop-blur-md border-b border-border shadow-sm transition-all duration-300">
        <div className="max-w-7xl mx-auto px-6 md:px-10 flex justify-between items-center h-16">
          <Link to="/" className="flex items-center space-x-2">
            <GraduationCap className="w-8 h-8 text-primary" />
            <span className="text-xl md:text-2xl font-bold text-primary tracking-tight">
              卓越学术机构
            </span>
          </Link>
          <div className="hidden md:flex items-center space-x-8">
            <Link
              to="/"
              className={cn(
                "text-base font-semibold transition-colors hover:text-primary",
                location.pathname === "/"
                  ? "text-primary border-b-2 border-primary pb-1"
                  : "text-muted"
              )}
            >
              首页
            </Link>
            <Link
              to="/courses"
              className={cn(
                "text-base font-semibold transition-colors hover:text-primary",
                location.pathname === "/courses"
                  ? "text-primary border-b-2 border-primary pb-1"
                  : "text-muted"
              )}
            >
              课程列表
            </Link>
            <Link
              to="/admin"
              className="text-base font-semibold text-muted hover:text-primary transition-colors"
            >
              管理后台体验
            </Link>
          </div>
          <button className="hidden md:flex items-center justify-center px-6 py-2.5 bg-primary text-primary-foreground rounded-lg font-medium text-sm hover:opacity-90 transition-opacity shadow-sm hover:shadow-md">
            立即报名
          </button>
        </div>
      </nav>

      <main className="flex-grow pt-16">
        <Outlet />
      </main>

      <footer className="w-full bg-surface-dim border-t border-border">
        <div className="max-w-7xl mx-auto px-10 py-16 grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="col-span-1">
            <span className="text-xl font-bold text-primary block mb-4">卓越学术机构</span>
            <p className="text-sm text-muted mb-6">
              通过严谨的学术卓越表现培养下一代行业领导者。
            </p>
            <p className="text-sm text-muted">
              &copy; 2026 Academic Distinction Institute. 保留所有权利。
            </p>
          </div>
          <div className="col-span-1 flex flex-col space-y-4">
            <h4 className="text-sm font-semibold uppercase tracking-wider text-foreground">机构</h4>
            <Link to="/" className="text-sm text-muted hover:underline">关于我们</Link>
            <Link to="/" className="text-sm text-muted hover:underline">领导团队</Link>
          </div>
          <div className="col-span-1 flex flex-col space-y-4">
            <h4 className="text-sm font-semibold uppercase tracking-wider text-foreground">资源</h4>
            <Link to="/" className="text-sm text-muted hover:underline">校园地图</Link>
            <Link to="/" className="text-sm text-muted hover:underline">学生门户</Link>
          </div>
          <div className="col-span-1 flex flex-col space-y-4">
            <h4 className="text-sm font-semibold uppercase tracking-wider text-foreground">法律</h4>
            <Link to="/" className="text-sm text-muted hover:underline">联系我们</Link>
            <Link to="/" className="text-sm text-muted hover:underline">隐私政策</Link>
          </div>
        </div>
      </footer>
    </div>
  );
}
