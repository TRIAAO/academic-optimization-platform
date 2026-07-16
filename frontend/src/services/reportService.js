import { optimizationService } from "./optimizationService";

export const reportService = {
  async generateByResearcher(researcherId) {
    return optimizationService.generateByResearcher(researcherId);
  },

  async findByResearcher(researcherId) {
    const report = await optimizationService.generateByResearcher(researcherId);
    return report ? [report] : [];
  },

  async findLatestByResearcher(researcherId) {
    return optimizationService.generateByResearcher(researcherId);
  },

  async downloadPdf({ researcherId }) {
    return optimizationService.downloadPdf(researcherId);
  }
};
